package com.jovanpets.service;

import com.jovanpets.document.Cliente;
import com.jovanpets.document.DetallePedido;
import com.jovanpets.document.Pedido;
import com.jovanpets.document.Producto;
import com.jovanpets.domain.EstadoPedido;
import com.jovanpets.domain.ModalidadEntrega;
import com.jovanpets.dto.CotizacionResponse;
import com.jovanpets.dto.LineaPedidoRequest;
import com.jovanpets.dto.PedidoRequest;
import com.jovanpets.dto.PedidoResponse;
import com.jovanpets.exception.AccesoDenegadoException;
import com.jovanpets.exception.ConflictoException;
import com.jovanpets.exception.NegocioException;
import com.jovanpets.exception.RecursoNoEncontradoException;
import com.jovanpets.mapper.PedidoMapper;
import com.jovanpets.repository.PedidoRepository;
import com.jovanpets.repository.ProductoRepository;
import com.jovanpets.repository.SecuenciaRepository;
import com.jovanpets.security.UsuarioAutenticado;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Carrito, pedidos y su ciclo de vida (HU-16, HU-17, RPE-04, RPE-05).
 *
 * <p>Consistencia sin transacciones multi-documento: cada descuento de stock es atomico; si un
 * producto no alcanza o falla el guardado, se compensan (devuelven) los descuentos ya hechos.
 * Funciona tanto en un MongoDB independiente como en un replica set.
 */
@Service
public class PedidoService {
  private final PedidoRepository pedidos;
  private final ProductoRepository productos;
  private final InventarioService inventario;
  private final ClienteService clientes;
  private final SecuenciaRepository secuencias;
  private final Clock reloj;

  public PedidoService(
      PedidoRepository pedidos,
      ProductoRepository productos,
      InventarioService inventario,
      ClienteService clientes,
      SecuenciaRepository secuencias,
      Clock reloj) {
    this.pedidos = pedidos;
    this.productos = productos;
    this.inventario = inventario;
    this.clientes = clientes;
    this.secuencias = secuencias;
    this.reloj = reloj;
  }

  /** HU-16: total del carrito calculado en el backend con precios y stock vigentes. */
  public CotizacionResponse cotizar(List<LineaPedidoRequest> lineas) {
    Map<String, Integer> cantidades = consolidar(lineas);
    Map<String, Producto> encontrados = cargarVendibles(cantidades);
    List<DetallePedido> detalles = new ArrayList<>();
    cantidades.forEach(
        (id, cantidad) -> {
          Producto p = encontrados.get(id);
          if (p.getStock() < cantidad) {
            throw new ConflictoException(
                "Stock insuficiente para " + p.getNombre() + " (disponible: " + p.getStock() + ")");
          }
          detalles.add(detalle(p, cantidad));
        });
    return new CotizacionResponse(detalles.stream().map(PedidoMapper::aResponse).toList(), total(detalles));
  }

  /** HU-17: confirma el pedido, descuenta el stock y lo registra con estado PENDIENTE. */
  public PedidoResponse crear(PedidoRequest r, UsuarioAutenticado actor) {
    String clienteId = actor.esCliente() ? actor.clienteId() : r.clienteId();
    NegocioException.exigir(clienteId != null && !clienteId.isBlank(), "Selecciona el cliente del pedido");
    Cliente cliente = clientes.obtenerActivo(clienteId);
    String direccion = null;
    if (r.modalidad() == ModalidadEntrega.DOMICILIO) {
      direccion = r.direccionEntrega() == null || r.direccionEntrega().isBlank() ? cliente.getDireccion() : r.direccionEntrega().trim();
      NegocioException.exigir(direccion != null && !direccion.isBlank(), "Indica la direccion de entrega");
    }

    Map<String, Integer> cantidades = consolidar(r.lineas());
    Map<String, Producto> vendibles = cargarVendibles(cantidades);

    List<Producto> reservados = new ArrayList<>();
    List<Integer> reservadas = new ArrayList<>();
    Pedido pedido = new Pedido();
    try {
      for (Map.Entry<String, Integer> linea : cantidades.entrySet()) {
        Producto p = inventario.reservar(linea.getKey(), linea.getValue(), vendibles.get(linea.getKey()).getNombre());
        reservados.add(p);
        reservadas.add(linea.getValue());
      }
      for (int i = 0; i < reservados.size(); i++) {
        pedido.getDetalles().add(detalle(reservados.get(i), reservadas.get(i)));
      }
      pedido.setTotal(total(pedido.getDetalles()));
      pedido.setNumero(String.format("JP-%d-%06d", LocalDateTime.now(reloj).getYear(), secuencias.siguiente("pedidos")));
      pedido.setClienteId(cliente.getId());
      pedido.setClienteNombre(cliente.getNombre());
      pedido.setFecha(LocalDateTime.now(reloj));
      pedido.setEstado(EstadoPedido.PENDIENTE);
      pedido.setModalidad(r.modalidad());
      pedido.setDireccionEntrega(direccion);
      pedido = pedidos.save(pedido);
      inventario.registrarSalidasPedido(pedido, reservados, actor);
      return PedidoMapper.aResponse(pedido);
    } catch (RuntimeException e) {
      for (int i = 0; i < reservados.size(); i++) {
        inventario.liberar(reservados.get(i).getId(), reservadas.get(i));
      }
      if (pedido.getId() != null) {
        pedidos.deleteById(pedido.getId());
      }
      throw e;
    }
  }

  public List<PedidoResponse> listar(EstadoPedido estado, UsuarioAutenticado actor) {
    List<Pedido> lista;
    if (actor.esCliente()) {
      lista = actor.clienteId() == null ? List.of() : pedidos.findByClienteIdOrderByFechaDesc(actor.clienteId());
      if (estado != null) {
        lista = lista.stream().filter(p -> p.getEstado() == estado).toList();
      }
    } else {
      lista = estado == null ? pedidos.findAllByOrderByFechaDesc() : pedidos.findByEstadoOrderByFechaDesc(estado);
    }
    return lista.stream().map(PedidoMapper::aResponse).toList();
  }

  public PedidoResponse obtener(String id, UsuarioAutenticado actor) {
    Pedido p = buscar(id);
    verificarAcceso(p, actor);
    return PedidoMapper.aResponse(p);
  }

  /** Avance de estado desde el panel administrativo. */
  public PedidoResponse cambiarEstado(String id, EstadoPedido destino, UsuarioAutenticado actor) {
    if (destino == EstadoPedido.CANCELADO) {
      return cancelar(id, "Cancelado por la veterinaria", actor);
    }
    Pedido p = buscar(id);
    NegocioException.exigir(p.getEstado().puedeCambiarA(destino), "No se puede pasar un pedido " + p.getEstado() + " a " + destino);
    p.setEstado(destino);
    return PedidoMapper.aResponse(pedidos.save(p));
  }

  /**
   * RPE-05: cancelar antes del despacho. El cambio de estado se guarda primero con bloqueo
   * optimista, asi dos cancelaciones simultaneas no reponen el stock dos veces.
   */
  public PedidoResponse cancelar(String id, String motivo, UsuarioAutenticado actor) {
    Pedido p = buscar(id);
    verificarAcceso(p, actor);
    NegocioException.exigir(p.getEstado().esCancelable(), "El pedido ya no se puede cancelar");
    p.setEstado(EstadoPedido.CANCELADO);
    p.setMotivoCancelacion(motivo.trim());
    Pedido guardado = pedidos.save(p);
    inventario.reponerPedido(guardado, actor);
    return PedidoMapper.aResponse(guardado);
  }

  public List<PedidoResponse> recientes() {
    return pedidos.findTop5ByOrderByFechaDesc().stream().map(PedidoMapper::aResponse).toList();
  }

  public long contarPendientes() {
    return pedidos.countByEstado(EstadoPedido.PENDIENTE);
  }

  /** Agrupa lineas repetidas del mismo producto. */
  static Map<String, Integer> consolidar(List<LineaPedidoRequest> lineas) {
    NegocioException.exigir(lineas != null && !lineas.isEmpty(), "El pedido debe tener al menos un producto");
    Map<String, Integer> cantidades = new LinkedHashMap<>();
    for (LineaPedidoRequest l : lineas) {
      cantidades.merge(l.productoId(), l.cantidad(), Integer::sum);
    }
    cantidades.values().forEach(c -> NegocioException.exigir(c <= 100, "La cantidad maxima por producto es 100"));
    return cantidades;
  }

  static BigDecimal total(List<DetallePedido> detalles) {
    return detalles.stream().map(DetallePedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Map<String, Producto> cargarVendibles(Map<String, Integer> cantidades) {
    Map<String, Producto> encontrados =
        productos.findAllById(cantidades.keySet()).stream().collect(Collectors.toMap(Producto::getId, Function.identity()));
    for (String id : cantidades.keySet()) {
      Producto p = encontrados.get(id);
      if (p == null || !p.isActivo()) {
        throw new RecursoNoEncontradoException("Producto");
      }
      NegocioException.exigir(
          !p.isRequiereFormula(),
          p.getNombre() + " requiere formula veterinaria; solicitalo en el establecimiento");
    }
    return encontrados;
  }

  private static DetallePedido detalle(Producto p, int cantidad) {
    DetallePedido d = new DetallePedido();
    d.setProductoId(p.getId());
    d.setNombreProducto(p.getNombre());
    d.setCantidad(cantidad);
    d.setPrecioUnitario(p.getPrecio());
    d.setSubtotal(p.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
    return d;
  }

  private void verificarAcceso(Pedido p, UsuarioAutenticado actor) {
    if (actor.esCliente() && !p.getClienteId().equals(actor.clienteId())) {
      throw new AccesoDenegadoException();
    }
  }

  private Pedido buscar(String id) {
    return pedidos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Pedido"));
  }
}

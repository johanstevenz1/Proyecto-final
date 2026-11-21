package com.jovanpets.service;

import com.jovanpets.document.DetallePedido;
import com.jovanpets.document.MovimientoInventario;
import com.jovanpets.document.Pedido;
import com.jovanpets.document.Producto;
import com.jovanpets.domain.TipoMovimiento;
import com.jovanpets.dto.MovimientoRequest;
import com.jovanpets.dto.MovimientoResponse;
import com.jovanpets.dto.ProductoResponse;
import com.jovanpets.exception.ConflictoException;
import com.jovanpets.exception.RecursoNoEncontradoException;
import com.jovanpets.mapper.MovimientoMapper;
import com.jovanpets.mapper.ProductoMapper;
import com.jovanpets.repository.MovimientoInventarioRepository;
import com.jovanpets.repository.ProductoRepository;
import com.jovanpets.security.UsuarioAutenticado;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Existencias e historial de movimientos (HU-14, HU-15). Toda variacion de stock pasa por
 * operaciones atomicas de MongoDB: una salida solo se aplica si stock >= cantidad, por lo que el
 * inventario nunca queda negativo aunque haya operaciones concurrentes.
 */
@Service
public class InventarioService {
  private final ProductoRepository productos;
  private final MovimientoInventarioRepository movimientos;
  private final Clock reloj;

  public InventarioService(ProductoRepository productos, MovimientoInventarioRepository movimientos, Clock reloj) {
    this.productos = productos;
    this.movimientos = movimientos;
    this.reloj = reloj;
  }

  /** HU-14: entrada incrementa y salida decrementa; no se permiten salidas mayores al stock. */
  public MovimientoResponse registrar(MovimientoRequest r, UsuarioAutenticado actor) {
    Producto actual = productos.findById(r.productoId()).orElseThrow(() -> new RecursoNoEncontradoException("Producto"));
    Producto actualizado =
        r.tipo() == TipoMovimiento.ENTRADA
            ? productos.incrementarStock(actual.getId(), r.cantidad()).orElseThrow(() -> new RecursoNoEncontradoException("Producto"))
            : productos
                .descontarStock(actual.getId(), r.cantidad())
                .orElseThrow(
                    () ->
                        new ConflictoException(
                            "Stock insuficiente para " + actual.getNombre() + " (disponible: " + actual.getStock() + ")"));
    LocalDateTime fecha = r.fecha() == null ? LocalDateTime.now(reloj) : r.fecha();
    return MovimientoMapper.aResponse(
        movimientos.save(movimiento(actualizado, r.tipo(), r.cantidad(), r.motivo().trim(), fecha, actor, null)));
  }

  public List<MovimientoResponse> historial(String productoId) {
    List<MovimientoInventario> lista =
        productoId == null || productoId.isBlank()
            ? movimientos.findTop100ByOrderByFechaDesc()
            : movimientos.findByProductoIdOrderByFechaDesc(productoId);
    return lista.stream().map(MovimientoMapper::aResponse).toList();
  }

  /** HU-15: productos con stock menor al minimo. */
  public List<ProductoResponse> alertas() {
    return productos.buscarStockBajo().stream().map(ProductoMapper::aResponse).toList();
  }

  /** Registra el stock inicial de un producto recien creado. */
  public void registrarStockInicial(Producto p, UsuarioAutenticado actor) {
    if (p.getStock() > 0) {
      movimientos.save(
          movimiento(p, TipoMovimiento.ENTRADA, p.getStock(), "Stock inicial", LocalDateTime.now(reloj), actor, null));
    }
  }

  /** Reserva existencias para un pedido de forma atomica o lanza conflicto si no alcanzan. */
  public Producto reservar(String productoId, int cantidad, String nombre) {
    return productos
        .descontarStock(productoId, cantidad)
        .orElseThrow(() -> new ConflictoException("Stock insuficiente para " + nombre));
  }

  /** Compensacion: devuelve existencias reservadas cuando el pedido no llega a guardarse. */
  public void liberar(String productoId, int cantidad) {
    productos.incrementarStock(productoId, cantidad);
  }

  /** Registra las salidas de un pedido confirmado (sincroniza inventario y pedidos). */
  public void registrarSalidasPedido(Pedido pedido, List<Producto> actualizados, UsuarioAutenticado actor) {
    for (int i = 0; i < pedido.getDetalles().size(); i++) {
      DetallePedido d = pedido.getDetalles().get(i);
      movimientos.save(
          movimiento(
              actualizados.get(i), TipoMovimiento.SALIDA, d.getCantidad(), "Pedido " + pedido.getNumero(),
              pedido.getFecha(), actor, pedido.getId()));
    }
  }

  /** Repone las existencias de un pedido cancelado y deja el movimiento de entrada. */
  public void reponerPedido(Pedido pedido, UsuarioAutenticado actor) {
    for (DetallePedido d : pedido.getDetalles()) {
      productos
          .incrementarStock(d.getProductoId(), d.getCantidad())
          .ifPresent(
              p ->
                  movimientos.save(
                      movimiento(
                          p, TipoMovimiento.ENTRADA, d.getCantidad(), "Cancelacion pedido " + pedido.getNumero(),
                          LocalDateTime.now(reloj), actor, pedido.getId())));
    }
  }

  private static MovimientoInventario movimiento(
      Producto p, TipoMovimiento tipo, int cantidad, String motivo, LocalDateTime fecha, UsuarioAutenticado actor,
      String pedidoId) {
    MovimientoInventario m = new MovimientoInventario();
    m.setProductoId(p.getId());
    m.setProductoNombre(p.getNombre());
    m.setTipo(tipo);
    m.setCantidad(cantidad);
    m.setMotivo(motivo);
    m.setFecha(fecha);
    m.setSaldo(p.getStock());
    m.setUsuarioId(actor.id());
    m.setUsuarioNombre(actor.nombre());
    m.setPedidoId(pedidoId);
    return m;
  }
}

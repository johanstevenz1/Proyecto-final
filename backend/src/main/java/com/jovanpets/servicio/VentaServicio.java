package com.jovanpets.servicio;

import static com.jovanpets.servicio.Reglas.*;

import com.jovanpets.controlador.Solicitudes.*;
import com.jovanpets.dominio.*;
import com.jovanpets.repositorio.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaServicio implements IVentas {
  private final ProductoRepositorio productos;
  private final MovimientoInventarioRepositorio movimientos;
  private final PedidoRepositorio pedidos;
  private final ClienteRepositorio clientes;
  private final Acceso acceso;

  public VentaServicio(
      ProductoRepositorio p,
      MovimientoInventarioRepositorio m,
      PedidoRepositorio r,
      ClienteRepositorio c,
      Acceso a) {
    productos = p;
    movimientos = m;
    pedidos = r;
    clientes = c;
    acceso = a;
  }

  private LocalDateTime ahora() {
    return LocalDateTime.now(ZoneId.of("America/Bogota"));
  }

  public List<Producto> productos(String q) {
    String consulta = q == null ? "" : q.toLowerCase(Locale.ROOT);
    return productos.findAll().stream()
        .filter(
            p ->
                p.activo
                    && (p.nombre.toLowerCase(Locale.ROOT).contains(consulta)
                        || p.categoria.toLowerCase(Locale.ROOT).contains(consulta)))
        .toList();
  }

  @Transactional
  public Producto guardarProducto(Long id, Producto e) {
    acceso.administrador();
    Producto p = id == null ? new Producto() : existe(productos.bloquear(id));
    p.nombre = texto(e.nombre, "Nombre", 120);
    p.descripcion = e.descripcion;
    p.categoria = texto(e.categoria, "Categoria", 80);
    p.precio = dinero(e.precio);
    exigir(p.precio.signum() > 0, "El precio del producto debe ser mayor que cero");
    exigir(e.stockMinimo >= 0 && e.stockMinimo <= 1000000, "Stock minimo fuera de rango");
    p.stockMinimo = e.stockMinimo;
    p.activo = e.activo;
    exigir(id != null || e.stock >= 0 && e.stock <= 1000000, "Stock inicial fuera de rango");
    if (id == null) p.stock = 0;
    productos.save(p);
    if (id == null && e.stock > 0)
      registrar(p, TipoMovimiento.ENTRADA, e.stock, "Existencia inicial");
    return p;
  }

  private MovimientoInventario registrar(
      Producto p, TipoMovimiento tipo, int cantidad, String motivo) {
    exigir(cantidad > 0, "La cantidad debe ser mayor que cero");
    long saldo = (long) p.stock + (tipo == TipoMovimiento.ENTRADA ? cantidad : -cantidad);
    conflicto(saldo < 0, "Existencias insuficientes para " + p.nombre);
    exigir(saldo <= 1000000, "El saldo supera el limite del prototipo");
    p.stock = (int) saldo;
    productos.save(p);
    MovimientoInventario m = new MovimientoInventario();
    m.producto = p;
    m.fecha = ahora();
    m.tipo = tipo;
    m.cantidad = cantidad;
    m.motivo = motivo;
    m.saldo = p.stock;
    return movimientos.save(m);
  }

  @Transactional
  public MovimientoInventario mover(Movimiento e) {
    acceso.personal();
    Producto p = existe(productos.bloquear(e.productoId()));
    return registrar(p, e.tipo(), e.cantidad(), texto(e.motivo(), "Motivo", 500));
  }

  public List<MovimientoInventario> movimientos() {
    acceso.personal();
    return movimientos.findAllByOrderByFechaDescIdDesc();
  }

  public List<Producto> alertas() {
    acceso.personal();
    return productos.findAll().stream().filter(p -> p.activo && p.stock < p.stockMinimo).toList();
  }

  public List<Pedido> pedidos() {
    var u = acceso.actual();
    return u.rol == Rol.CLIENTE
        ? pedidos.findByClienteIdOrderByFechaDesc(u.cliente.id)
        : pedidos.findAll().stream()
            .sorted(Comparator.comparing((Pedido p) -> p.fecha).reversed())
            .toList();
  }

  @Transactional
  public Pedido comprar(Compra e) {
    acceso.comprador();
    var u = acceso.actual();
    Long clienteId = u.rol == Rol.CLIENTE ? u.cliente.id : e.clienteId();
    exigir(clienteId != null, "Selecciona el cliente del pedido");
    Cliente c = existe(clientes.findById(clienteId));
    exigir(c.activo, "Cliente inactivo");
    Pedido p = new Pedido();
    p.cliente = c;
    p.fecha = ahora();
    p.modalidad = e.modalidad();
    p.direccionEntrega =
        e.modalidad() == ModalidadEntrega.DOMICILIO
            ? texto(e.direccionEntrega(), "Direccion de entrega", 250)
            : null;
    Map<Long, Integer> cantidades = new TreeMap<>();
    for (Linea l : e.lineas()) cantidades.merge(l.productoId(), l.cantidad(), Integer::sum);
    for (var item : cantidades.entrySet()) {
      exigir(item.getValue() <= 10000, "Cantidad por producto fuera de rango");
      Producto pr = existe(productos.bloquear(item.getKey()));
      exigir(pr.activo, "Producto inactivo");
      DetallePedido det = new DetallePedido();
      det.pedido = p;
      det.producto = pr;
      det.nombreProducto = pr.nombre;
      det.cantidad = item.getValue();
      det.precioUnitario = pr.precio;
      det.subtotal = pr.precio.multiply(BigDecimal.valueOf(det.cantidad));
      p.total = p.total.add(det.subtotal);
      p.detalles.add(det);
    }
    exigir(p.total.precision() <= 14, "El total supera el limite del pedido");
    pedidos.save(p);
    for (DetallePedido det : p.detalles)
      registrar(det.producto, TipoMovimiento.SALIDA, det.cantidad, "Pedido #" + p.id);
    return p;
  }

  @Transactional
  public Pedido cambiarPedido(Long id, EstadoPedido siguiente) {
    Pedido p = existe(pedidos.bloquear(id));
    acceso.propietario(p.cliente.id);
    if (siguiente == EstadoPedido.CANCELADO) {
      exigir(
          Set.of(EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO_RECOGER)
              .contains(p.estado),
          "El pedido ya no se puede cancelar");
      List<DetallePedido> detalles =
          p.detalles.stream().sorted(Comparator.comparing(d -> d.producto.id)).toList();
      for (DetallePedido d : detalles)
        registrar(
            existe(productos.bloquear(d.producto.id)),
            TipoMovimiento.ENTRADA,
            d.cantidad,
            "Cancelacion pedido #" + p.id);
    } else {
      acceso.personal();
      boolean valido =
          p.estado == EstadoPedido.PENDIENTE && siguiente == EstadoPedido.EN_PREPARACION
              || p.estado == EstadoPedido.EN_PREPARACION
                  && (p.modalidad == ModalidadEntrega.RECOGIDA
                      ? siguiente == EstadoPedido.LISTO_RECOGER
                      : siguiente == EstadoPedido.DESPACHADO)
              || (p.estado == EstadoPedido.LISTO_RECOGER || p.estado == EstadoPedido.DESPACHADO)
                  && siguiente == EstadoPedido.ENTREGADO;
      exigir(valido, "Transicion de pedido no permitida");
    }
    p.estado = siguiente;
    return pedidos.save(p);
  }
}

package com.jovanpets.servicio;

import com.jovanpets.controlador.Solicitudes.*;
import com.jovanpets.dominio.*;
import java.util.List;

public interface IVentas {
  List<Producto> productos(String q);

  Producto guardarProducto(Long id, Producto entrada);

  MovimientoInventario mover(Movimiento entrada);

  List<MovimientoInventario> movimientos();

  List<Producto> alertas();

  Pedido comprar(Compra entrada);

  List<Pedido> pedidos();

  Pedido cambiarPedido(Long id, EstadoPedido estado);
}

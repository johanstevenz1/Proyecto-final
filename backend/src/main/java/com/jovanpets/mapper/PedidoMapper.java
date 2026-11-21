package com.jovanpets.mapper;

import com.jovanpets.document.DetallePedido;
import com.jovanpets.document.Pedido;
import com.jovanpets.dto.DetallePedidoResponse;
import com.jovanpets.dto.PedidoResponse;

public final class PedidoMapper {
  private PedidoMapper() {}

  public static DetallePedidoResponse aResponse(DetallePedido d) {
    return new DetallePedidoResponse(
        d.getProductoId(), d.getNombreProducto(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
  }

  public static PedidoResponse aResponse(Pedido p) {
    return new PedidoResponse(
        p.getId(), p.getNumero(), p.getClienteId(), p.getClienteNombre(), p.getFecha(), p.getEstado(),
        p.getModalidad(), p.getDireccionEntrega(),
        p.getDetalles().stream().map(PedidoMapper::aResponse).toList(), p.getTotal(), p.getMotivoCancelacion());
  }
}

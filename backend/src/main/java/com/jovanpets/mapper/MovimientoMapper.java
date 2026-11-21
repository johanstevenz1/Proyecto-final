package com.jovanpets.mapper;

import com.jovanpets.document.MovimientoInventario;
import com.jovanpets.dto.MovimientoResponse;

public final class MovimientoMapper {
  private MovimientoMapper() {}

  public static MovimientoResponse aResponse(MovimientoInventario m) {
    return new MovimientoResponse(
        m.getId(), m.getProductoId(), m.getProductoNombre(), m.getFecha(), m.getTipo(), m.getCantidad(),
        m.getMotivo(), m.getSaldo(), m.getUsuarioNombre(), m.getPedidoId());
  }
}

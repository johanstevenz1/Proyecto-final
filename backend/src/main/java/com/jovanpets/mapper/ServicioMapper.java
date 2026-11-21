package com.jovanpets.mapper;

import com.jovanpets.document.Servicio;
import com.jovanpets.dto.ServicioResponse;

public final class ServicioMapper {
  private ServicioMapper() {}

  public static ServicioResponse aResponse(Servicio s) {
    return new ServicioResponse(
        s.getId(), s.getNombre(), s.getDescripcion(), s.getCategoria(), s.getPrecio(), s.getDuracionMinutos(),
        s.isActivo());
  }
}

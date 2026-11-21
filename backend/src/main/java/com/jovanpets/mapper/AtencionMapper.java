package com.jovanpets.mapper;

import com.jovanpets.document.Atencion;
import com.jovanpets.dto.AtencionResponse;

public final class AtencionMapper {
  private AtencionMapper() {}

  public static AtencionResponse aResponse(Atencion a) {
    return new AtencionResponse(
        a.getId(), a.getMascotaId(), a.getTipo(), a.getFecha(), a.getDescripcion(), a.getObservaciones(),
        a.getProfesional(), a.getProximaFecha());
  }
}

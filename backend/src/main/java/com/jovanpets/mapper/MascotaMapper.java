package com.jovanpets.mapper;

import com.jovanpets.document.Mascota;
import com.jovanpets.dto.MascotaResponse;

public final class MascotaMapper {
  private MascotaMapper() {}

  public static MascotaResponse aResponse(Mascota m, String propietarioNombre) {
    return new MascotaResponse(
        m.getId(), m.getPropietarioId(), propietarioNombre, m.getNombre(), m.getEspecie(), m.getRaza(), m.getSexo(),
        m.getFechaNacimiento(), m.getPeso(), m.isActivo());
  }
}

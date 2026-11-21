package com.jovanpets.mapper;

import com.jovanpets.document.Cita;
import com.jovanpets.dto.CitaResponse;

public final class CitaMapper {
  private CitaMapper() {}

  public static CitaResponse aResponse(Cita c) {
    return new CitaResponse(
        c.getId(), c.getMascotaId(), c.getMascotaNombre(), c.getClienteId(), c.getClienteNombre(), c.getServicioId(),
        c.getServicioNombre(), c.getAgenda(), c.getInicio(), c.getFin(), c.getEstado(), c.getMotivo(),
        c.getVeterinarioId(), c.getVeterinarioNombre());
  }
}

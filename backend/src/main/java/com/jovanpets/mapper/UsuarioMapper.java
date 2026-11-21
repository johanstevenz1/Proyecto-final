package com.jovanpets.mapper;

import com.jovanpets.document.Usuario;
import com.jovanpets.dto.UsuarioResponse;

public final class UsuarioMapper {
  private UsuarioMapper() {}

  public static UsuarioResponse aResponse(Usuario u) {
    return new UsuarioResponse(
        u.getId(), u.getEmail(), u.getNombre(), u.getRol(), u.isActivo(), u.getClienteId(), u.getFechaRegistro());
  }
}

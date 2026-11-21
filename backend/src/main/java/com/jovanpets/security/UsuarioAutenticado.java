package com.jovanpets.security;

import com.jovanpets.domain.Rol;

/**
 * Datos del usuario autenticado en la peticion actual. Se construyen en cada solicitud a partir
 * de MongoDB, de modo que un cambio de rol o una desactivacion tienen efecto inmediato (HU-20).
 */
public record UsuarioAutenticado(String id, String email, String nombre, Rol rol, String clienteId) {

  public boolean esCliente() {
    return rol == Rol.CLIENTE;
  }

  public boolean esPersonal() {
    return rol.esPersonal();
  }

  public boolean esAdministrador() {
    return rol == Rol.ADMINISTRADOR;
  }
}

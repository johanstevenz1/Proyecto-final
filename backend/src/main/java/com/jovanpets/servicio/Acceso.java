package com.jovanpets.servicio;

import com.jovanpets.dominio.*;
import com.jovanpets.repositorio.UsuarioRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class Acceso {
  private final UsuarioRepositorio usuarios;

  public Acceso(UsuarioRepositorio usuarios) {
    this.usuarios = usuarios;
  }

  public Usuario actual() {
    var a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null || !a.isAuthenticated())
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesion");
    return usuarios
        .findByEmailIgnoreCase(a.getName())
        .filter(u -> u.activo)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cuenta inactiva"));
  }

  public boolean esPersonal() {
    return actual().rol != Rol.CLIENTE;
  }

  public void personal() {
    if (!esPersonal()) prohibir();
  }

  public void administrador() {
    if (actual().rol != Rol.ADMINISTRADOR) prohibir();
  }

  public void comprador() {
    if (actual().rol == Rol.PERSONAL) prohibir();
  }

  public void propietario(Long id) {
    Usuario u = actual();
    if (u.rol == Rol.CLIENTE && (u.cliente == null || !u.cliente.id.equals(id))) prohibir();
  }

  public void prohibir() {
    throw new ResponseStatusException(
        HttpStatus.FORBIDDEN, "No tienes permiso para esta operacion");
  }
}

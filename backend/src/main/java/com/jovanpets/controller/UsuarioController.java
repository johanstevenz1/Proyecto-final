package com.jovanpets.controller;

import com.jovanpets.domain.Rol;
import com.jovanpets.dto.CambioEstadoRequest;
import com.jovanpets.dto.CambioRolRequest;
import com.jovanpets.dto.UsuarioCrearRequest;
import com.jovanpets.dto.UsuarioResponse;
import com.jovanpets.security.UsuarioAutenticado;
import com.jovanpets.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Usuarios y roles: HU-20, ROL-04 y ROL-05. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
  private final UsuarioService usuarios;

  public UsuarioController(UsuarioService usuarios) {
    this.usuarios = usuarios;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public List<UsuarioResponse> listar(@RequestParam(required = false) Rol rol) {
    return usuarios.listar(rol);
  }

  @GetMapping("/personal")
  @PreAuthorize("hasAnyRole('VETERINARIO','ADMINISTRADOR')")
  public List<UsuarioResponse> personal() {
    return usuarios.personal();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public UsuarioResponse crearPersonal(@Valid @RequestBody UsuarioCrearRequest r) {
    return usuarios.crearPersonal(r);
  }

  @PatchMapping("/{id}/rol")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public UsuarioResponse cambiarRol(
      @PathVariable String id, @Valid @RequestBody CambioRolRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return usuarios.cambiarRol(id, r.rol(), actor);
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public UsuarioResponse cambiarEstado(
      @PathVariable String id, @Valid @RequestBody CambioEstadoRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return usuarios.cambiarEstado(id, r.activo(), actor);
  }
}

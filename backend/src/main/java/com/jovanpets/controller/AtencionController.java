package com.jovanpets.controller;

import com.jovanpets.dto.AtencionRequest;
import com.jovanpets.dto.AtencionResponse;
import com.jovanpets.security.UsuarioAutenticado;
import com.jovanpets.service.AtencionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Edicion y eliminacion de entradas del historial clinico (HU-06, criterio 4). */
@RestController
@RequestMapping("/api/atenciones")
@PreAuthorize("hasAnyRole('VETERINARIO','ADMINISTRADOR')")
public class AtencionController {
  private final AtencionService atenciones;

  public AtencionController(AtencionService atenciones) {
    this.atenciones = atenciones;
  }

  @PutMapping("/{id}")
  public AtencionResponse actualizar(
      @PathVariable String id, @Valid @RequestBody AtencionRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return atenciones.actualizar(id, r, actor);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable String id) {
    atenciones.eliminar(id);
  }
}

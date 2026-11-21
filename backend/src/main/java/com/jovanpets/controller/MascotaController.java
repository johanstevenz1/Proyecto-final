package com.jovanpets.controller;

import com.jovanpets.dto.AtencionRequest;
import com.jovanpets.dto.AtencionResponse;
import com.jovanpets.dto.MascotaRequest;
import com.jovanpets.dto.MascotaResponse;
import com.jovanpets.security.UsuarioAutenticado;
import com.jovanpets.service.AtencionService;
import com.jovanpets.service.MascotaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Mascotas (HU-05) y su historial clinico (HU-06 a HU-08). */
@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {
  private final MascotaService mascotas;
  private final AtencionService atenciones;

  public MascotaController(MascotaService mascotas, AtencionService atenciones) {
    this.mascotas = mascotas;
    this.atenciones = atenciones;
  }

  @GetMapping
  public List<MascotaResponse> listar(
      @RequestParam(required = false) String propietarioId, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return mascotas.listar(propietarioId, actor);
  }

  @GetMapping("/{id}")
  public MascotaResponse obtener(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return mascotas.obtener(id, actor);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MascotaResponse crear(@Valid @RequestBody MascotaRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return mascotas.crear(r, actor);
  }

  @PutMapping("/{id}")
  public MascotaResponse actualizar(
      @PathVariable String id, @Valid @RequestBody MascotaRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return mascotas.actualizar(id, r, actor);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void desactivar(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado actor) {
    mascotas.desactivar(id, actor);
  }

  @GetMapping("/{id}/atenciones")
  public List<AtencionResponse> historial(
      @PathVariable String id,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
      @AuthenticationPrincipal UsuarioAutenticado actor) {
    return atenciones.historial(id, desde, hasta, actor);
  }

  @PostMapping("/{id}/atenciones")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('VETERINARIO','ADMINISTRADOR')")
  public AtencionResponse registrarAtencion(
      @PathVariable String id, @Valid @RequestBody AtencionRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return atenciones.registrar(id, r, actor);
  }
}

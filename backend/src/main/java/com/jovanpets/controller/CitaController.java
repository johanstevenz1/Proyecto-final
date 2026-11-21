package com.jovanpets.controller;

import com.jovanpets.domain.EstadoCita;
import com.jovanpets.dto.CitaRequest;
import com.jovanpets.dto.CitaResponse;
import com.jovanpets.dto.DisponibilidadResponse;
import com.jovanpets.dto.MotivoRequest;
import com.jovanpets.dto.ReprogramarCitaRequest;
import com.jovanpets.security.UsuarioAutenticado;
import com.jovanpets.service.CitaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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

/** Citas: HU-09, HU-10, HU-11, HU-23 y RA-07. */
@RestController
@RequestMapping("/api/citas")
public class CitaController {
  private final CitaService citas;

  public CitaController(CitaService citas) {
    this.citas = citas;
  }

  @GetMapping("/disponibilidad")
  public DisponibilidadResponse disponibilidad(
      @RequestParam String servicioId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
    return citas.disponibilidad(servicioId, fecha);
  }

  @GetMapping
  public List<CitaResponse> listar(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
      @RequestParam(required = false) EstadoCita estado,
      @RequestParam(required = false) String veterinarioId,
      @AuthenticationPrincipal UsuarioAutenticado actor) {
    return citas.listar(desde, hasta, estado, veterinarioId, actor);
  }

  @GetMapping("/{id}")
  public CitaResponse obtener(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return citas.obtener(id, actor);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('CLIENTE','ADMINISTRADOR')")
  public CitaResponse solicitar(@Valid @RequestBody CitaRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return citas.solicitar(r, actor);
  }

  @PatchMapping("/{id}/confirmar")
  @PreAuthorize("hasAnyRole('VETERINARIO','ADMINISTRADOR')")
  public CitaResponse confirmar(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return citas.confirmar(id, actor);
  }

  @PatchMapping("/{id}/rechazar")
  @PreAuthorize("hasAnyRole('VETERINARIO','ADMINISTRADOR')")
  public CitaResponse rechazar(
      @PathVariable String id, @Valid @RequestBody MotivoRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return citas.rechazar(id, r.motivo(), actor);
  }

  @PatchMapping("/{id}/atender")
  @PreAuthorize("hasAnyRole('VETERINARIO','ADMINISTRADOR')")
  public CitaResponse atender(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return citas.marcarAtendida(id, actor);
  }

  @PatchMapping("/{id}/cancelar")
  public CitaResponse cancelar(
      @PathVariable String id, @Valid @RequestBody MotivoRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return citas.cancelar(id, r.motivo(), actor);
  }

  @PatchMapping("/{id}/reprogramar")
  public CitaResponse reprogramar(
      @PathVariable String id,
      @Valid @RequestBody ReprogramarCitaRequest r,
      @AuthenticationPrincipal UsuarioAutenticado actor) {
    return citas.reprogramar(id, r.inicio(), actor);
  }
}

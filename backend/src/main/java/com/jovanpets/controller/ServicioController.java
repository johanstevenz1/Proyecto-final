package com.jovanpets.controller;

import com.jovanpets.dto.CambioEstadoRequest;
import com.jovanpets.dto.ServicioRequest;
import com.jovanpets.dto.ServicioResponse;
import com.jovanpets.service.ServicioCatalogoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Servicios veterinarios y de peluqueria (HU-12). */
@RestController
@RequestMapping("/api/servicios")
public class ServicioController {
  private final ServicioCatalogoService servicios;

  public ServicioController(ServicioCatalogoService servicios) {
    this.servicios = servicios;
  }

  /** Publico: servicios activos visibles para los clientes. */
  @GetMapping
  public List<ServicioResponse> activos() {
    return servicios.activos();
  }

  @GetMapping("/gestion")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public List<ServicioResponse> todos() {
    return servicios.todos();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ServicioResponse crear(@Valid @RequestBody ServicioRequest r) {
    return servicios.crear(r);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ServicioResponse actualizar(@PathVariable String id, @Valid @RequestBody ServicioRequest r) {
    return servicios.actualizar(id, r);
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ServicioResponse cambiarEstado(@PathVariable String id, @Valid @RequestBody CambioEstadoRequest r) {
    return servicios.cambiarEstado(id, r.activo());
  }
}

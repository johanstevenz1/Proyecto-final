package com.jovanpets.controller;

import com.jovanpets.dto.PanelResponse;
import com.jovanpets.service.EstadoService;
import com.jovanpets.service.PanelService;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dashboard (HU-24) y estado del sistema. */
@RestController
@RequestMapping("/api")
public class PanelController {
  private final PanelService panel;
  private final EstadoService estado;

  public PanelController(PanelService panel, EstadoService estado) {
    this.panel = panel;
    this.estado = estado;
  }

  @GetMapping("/panel")
  @PreAuthorize("hasAnyRole('VETERINARIO','ADMINISTRADOR')")
  public PanelResponse resumen() {
    return panel.resumen();
  }

  @GetMapping("/estado")
  public Map<String, String> estado() {
    return estado.estado();
  }
}

package com.jovanpets.controller;

import com.jovanpets.dto.MovimientoRequest;
import com.jovanpets.dto.MovimientoResponse;
import com.jovanpets.dto.ProductoResponse;
import com.jovanpets.security.UsuarioAutenticado;
import com.jovanpets.service.InventarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Inventario: HU-14 y HU-15. */
@RestController
@RequestMapping("/api/inventario")
@PreAuthorize("hasAnyRole('VETERINARIO','ADMINISTRADOR')")
public class InventarioController {
  private final InventarioService inventario;

  public InventarioController(InventarioService inventario) {
    this.inventario = inventario;
  }

  @PostMapping("/movimientos")
  @ResponseStatus(HttpStatus.CREATED)
  public MovimientoResponse registrar(
      @Valid @RequestBody MovimientoRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return inventario.registrar(r, actor);
  }

  @GetMapping("/movimientos")
  public List<MovimientoResponse> historial(@RequestParam(required = false) String productoId) {
    return inventario.historial(productoId);
  }

  @GetMapping("/alertas")
  public List<ProductoResponse> alertas() {
    return inventario.alertas();
  }
}

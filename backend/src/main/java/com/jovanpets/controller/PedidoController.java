package com.jovanpets.controller;

import com.jovanpets.domain.EstadoPedido;
import com.jovanpets.dto.CambioEstadoPedidoRequest;
import com.jovanpets.dto.CotizacionRequest;
import com.jovanpets.dto.CotizacionResponse;
import com.jovanpets.dto.MotivoRequest;
import com.jovanpets.dto.PedidoRequest;
import com.jovanpets.dto.PedidoResponse;
import com.jovanpets.security.UsuarioAutenticado;
import com.jovanpets.service.PedidoService;
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

/** Carrito y pedidos: HU-16, HU-17, RPE-04 y RPE-05. */
@RestController
@RequestMapping("/api/pedidos")
@PreAuthorize("hasAnyRole('CLIENTE','ADMINISTRADOR')")
public class PedidoController {
  private final PedidoService pedidos;

  public PedidoController(PedidoService pedidos) {
    this.pedidos = pedidos;
  }

  @PostMapping("/cotizar")
  public CotizacionResponse cotizar(@Valid @RequestBody CotizacionRequest r) {
    return pedidos.cotizar(r.lineas());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PedidoResponse crear(@Valid @RequestBody PedidoRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return pedidos.crear(r, actor);
  }

  @GetMapping
  public List<PedidoResponse> listar(
      @RequestParam(required = false) EstadoPedido estado, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return pedidos.listar(estado, actor);
  }

  @GetMapping("/{id}")
  public PedidoResponse obtener(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return pedidos.obtener(id, actor);
  }

  @PatchMapping("/{id}/cancelar")
  public PedidoResponse cancelar(
      @PathVariable String id, @Valid @RequestBody MotivoRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return pedidos.cancelar(id, r.motivo(), actor);
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public PedidoResponse cambiarEstado(
      @PathVariable String id,
      @Valid @RequestBody CambioEstadoPedidoRequest r,
      @AuthenticationPrincipal UsuarioAutenticado actor) {
    return pedidos.cambiarEstado(id, r.estado(), actor);
  }
}

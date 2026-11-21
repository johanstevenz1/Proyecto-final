package com.jovanpets.controller;

import com.jovanpets.dto.ClienteDetalleResponse;
import com.jovanpets.dto.ClienteRequest;
import com.jovanpets.dto.ClienteResponse;
import com.jovanpets.dto.PaginaResponse;
import com.jovanpets.dto.PerfilClienteRequest;
import com.jovanpets.security.UsuarioAutenticado;
import com.jovanpets.service.ClienteService;
import jakarta.validation.Valid;
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

/** Clientes: HU-01 a HU-04 y RC-05; perfil propio del cliente. */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
  private final ClienteService clientes;

  public ClienteController(ClienteService clientes) {
    this.clientes = clientes;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('VETERINARIO','ADMINISTRADOR')")
  public PaginaResponse<ClienteResponse> buscar(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "10") int tamano,
      @RequestParam(defaultValue = "nombre") String orden) {
    return clientes.buscar(q, pagina, tamano, orden);
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('CLIENTE')")
  public ClienteDetalleResponse perfil(@AuthenticationPrincipal UsuarioAutenticado actor) {
    return clientes.perfil(actor);
  }

  @PutMapping("/me")
  @PreAuthorize("hasRole('CLIENTE')")
  public ClienteResponse actualizarPerfil(
      @Valid @RequestBody PerfilClienteRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return clientes.actualizarPerfil(r, actor);
  }

  @GetMapping("/{id}")
  public ClienteDetalleResponse detalle(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return clientes.detalle(id, actor);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ClienteResponse crear(@Valid @RequestBody ClienteRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return clientes.crear(r, actor);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ClienteResponse actualizar(
      @PathVariable String id, @Valid @RequestBody ClienteRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return clientes.actualizar(id, r, actor);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public void desactivar(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado actor) {
    clientes.desactivar(id, actor);
  }
}

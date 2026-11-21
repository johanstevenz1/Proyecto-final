package com.jovanpets.controller;

import com.jovanpets.domain.CategoriaProducto;
import com.jovanpets.dto.CambioEstadoRequest;
import com.jovanpets.dto.ProductoRequest;
import com.jovanpets.dto.ProductoResponse;
import com.jovanpets.security.UsuarioAutenticado;
import com.jovanpets.service.ProductoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Catalogo de productos y medicamentos (HU-13, HU-16). */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {
  private final ProductoService productos;

  public ProductoController(ProductoService productos) {
    this.productos = productos;
  }

  @GetMapping
  public List<ProductoResponse> catalogo(
      @RequestParam(required = false) String q, @RequestParam(required = false) CategoriaProducto categoria) {
    return productos.catalogo(q, categoria);
  }

  @GetMapping("/gestion")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public List<ProductoResponse> gestion(
      @RequestParam(required = false) String q, @RequestParam(required = false) CategoriaProducto categoria) {
    return productos.gestion(q, categoria);
  }

  @GetMapping("/{id}")
  public ProductoResponse obtener(@PathVariable String id) {
    return productos.obtenerPublico(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ProductoResponse crear(@Valid @RequestBody ProductoRequest r, @AuthenticationPrincipal UsuarioAutenticado actor) {
    return productos.crear(r, actor);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ProductoResponse actualizar(@PathVariable String id, @Valid @RequestBody ProductoRequest r) {
    return productos.actualizar(id, r);
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ProductoResponse cambiarEstado(@PathVariable String id, @Valid @RequestBody CambioEstadoRequest r) {
    return productos.cambiarEstado(id, r.activo());
  }
}

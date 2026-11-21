package com.jovanpets.service;

import com.jovanpets.document.Producto;
import com.jovanpets.domain.CategoriaProducto;
import com.jovanpets.dto.ProductoRequest;
import com.jovanpets.dto.ProductoResponse;
import com.jovanpets.exception.RecursoNoEncontradoException;
import com.jovanpets.mapper.ProductoMapper;
import com.jovanpets.repository.ProductoRepository;
import com.jovanpets.security.UsuarioAutenticado;
import java.util.List;
import org.springframework.stereotype.Service;

/** Catalogo de productos y medicamentos (HU-13, HU-16, RP-01 a RP-05). */
@Service
public class ProductoService {
  private final ProductoRepository productos;
  private final InventarioService inventario;

  public ProductoService(ProductoRepository productos, InventarioService inventario) {
    this.productos = productos;
    this.inventario = inventario;
  }

  /** Catalogo publico: solo productos activos, con busqueda por nombre y categoria. */
  public List<ProductoResponse> catalogo(String texto, CategoriaProducto categoria) {
    return productos.buscar(texto, categoria, true).stream().map(ProductoMapper::aResponse).toList();
  }

  /** Listado administrativo, incluye inactivos. */
  public List<ProductoResponse> gestion(String texto, CategoriaProducto categoria) {
    return productos.buscar(texto, categoria, false).stream().map(ProductoMapper::aResponse).toList();
  }

  public ProductoResponse obtenerPublico(String id) {
    Producto p = buscar(id);
    if (!p.isActivo()) {
      throw new RecursoNoEncontradoException("Producto");
    }
    return ProductoMapper.aResponse(p);
  }

  /** HU-13: precio mayor a cero y stock inicial no negativo (validados en el DTO). */
  public ProductoResponse crear(ProductoRequest r, UsuarioAutenticado actor) {
    Producto p = new Producto();
    copiar(r, p);
    p.setStock(r.stockInicial() == null ? 0 : r.stockInicial());
    Producto guardado = productos.save(p);
    inventario.registrarStockInicial(guardado, actor);
    return ProductoMapper.aResponse(guardado);
  }

  /** Edita datos y precio. El stock no se toca aqui: solo cambia con movimientos de inventario. */
  public ProductoResponse actualizar(String id, ProductoRequest r) {
    Producto p = buscar(id);
    copiar(r, p);
    return ProductoMapper.aResponse(productos.save(p)); // @Version detecta ediciones concurrentes
  }

  public ProductoResponse cambiarEstado(String id, boolean activo) {
    Producto p = buscar(id);
    p.setActivo(activo);
    return ProductoMapper.aResponse(productos.save(p));
  }

  private Producto buscar(String id) {
    return productos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto"));
  }

  private static void copiar(ProductoRequest r, Producto p) {
    p.setNombre(r.nombre().trim());
    p.setDescripcion(r.descripcion().trim());
    p.setCategoria(r.categoria());
    p.setPrecio(r.precio());
    p.setStockMinimo(r.stockMinimo());
    p.setRequiereFormula(r.requiereFormula());
  }
}

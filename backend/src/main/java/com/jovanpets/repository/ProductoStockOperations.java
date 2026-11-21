package com.jovanpets.repository;

import com.jovanpets.document.Producto;
import com.jovanpets.domain.CategoriaProducto;
import java.util.List;
import java.util.Optional;

/** Operaciones de stock atomicas que no se pueden expresar con consultas derivadas. */
public interface ProductoStockOperations {

  /**
   * Resta la cantidad solo si hay existencias suficientes, en una unica operacion atomica de
   * MongoDB (findAndModify con condicion stock >= cantidad). Devuelve vacio si no alcanza.
   */
  Optional<Producto> descontarStock(String productoId, int cantidad);

  /** Suma existencias de forma atomica y devuelve el documento actualizado. */
  Optional<Producto> incrementarStock(String productoId, int cantidad);

  /** Catalogo con filtros opcionales de texto y categoria (HU-16). */
  List<Producto> buscar(String texto, CategoriaProducto categoria, boolean soloActivos);
}

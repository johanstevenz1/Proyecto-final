package com.jovanpets.mapper;

import com.jovanpets.document.Producto;
import com.jovanpets.dto.ProductoResponse;

public final class ProductoMapper {
  private ProductoMapper() {}

  public static ProductoResponse aResponse(Producto p) {
    return new ProductoResponse(
        p.getId(), p.getNombre(), p.getDescripcion(), p.getCategoria(), p.getPrecio(), p.getStock(),
        p.getStockMinimo(), p.stockBajo(), p.isRequiereFormula(), p.isActivo());
  }
}

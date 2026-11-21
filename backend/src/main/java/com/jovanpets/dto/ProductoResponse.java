package com.jovanpets.dto;

import com.jovanpets.domain.CategoriaProducto;
import java.math.BigDecimal;

public record ProductoResponse(
    String id, String nombre, String descripcion, CategoriaProducto categoria, BigDecimal precio, int stock,
    int stockMinimo, boolean stockBajo, boolean requiereFormula, boolean activo) {}

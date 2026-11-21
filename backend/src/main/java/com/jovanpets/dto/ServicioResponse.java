package com.jovanpets.dto;

import com.jovanpets.domain.CategoriaServicio;
import java.math.BigDecimal;

public record ServicioResponse(
    String id, String nombre, String descripcion, CategoriaServicio categoria, BigDecimal precio,
    int duracionMinutos, boolean activo) {}

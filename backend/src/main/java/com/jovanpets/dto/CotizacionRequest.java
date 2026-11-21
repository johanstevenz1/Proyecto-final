package com.jovanpets.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Carrito a cotizar: el total siempre se calcula en el backend. */
public record CotizacionRequest(
    @NotEmpty(message = "El carrito esta vacio") @Size(max = 50) List<@Valid LineaPedidoRequest> lineas) {}

package com.jovanpets.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Producto y cantidad del carrito. */
public record LineaPedidoRequest(
    @NotBlank(message = "Producto no valido") String productoId,
    @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad minima es 1") @Max(value = 100, message = "La cantidad maxima por producto es 100") Integer cantidad) {}

package com.jovanpets.dto;

import com.jovanpets.domain.CategoriaProducto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * HU-13: datos del producto. stockInicial solo se usa al crear; despues el stock cambia
 * exclusivamente mediante movimientos de inventario.
 */
public record ProductoRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombre,
    @NotBlank(message = "La descripcion es obligatoria") @Size(max = 500) String descripcion,
    @NotNull(message = "La categoria es obligatoria") CategoriaProducto categoria,
    @NotNull(message = "El precio es obligatorio") @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero") @Digits(integer = 10, fraction = 2) BigDecimal precio,
    @Min(value = 0, message = "El stock no puede ser negativo") @Max(100000) Integer stockInicial,
    @NotNull(message = "El stock minimo es obligatorio") @Min(value = 0, message = "El stock minimo no puede ser negativo") @Max(100000) Integer stockMinimo,
    boolean requiereFormula) {}

package com.jovanpets.dto;

import com.jovanpets.domain.CategoriaServicio;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** HU-12: datos del servicio. */
public record ServicioRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
    @NotBlank(message = "La descripcion es obligatoria") @Size(max = 500) String descripcion,
    @NotNull(message = "La categoria es obligatoria") CategoriaServicio categoria,
    @NotNull(message = "El precio es obligatorio") @DecimalMin(value = "0", message = "El precio no puede ser negativo") @Digits(integer = 10, fraction = 2) BigDecimal precio,
    @NotNull(message = "La duracion es obligatoria") @Min(value = 15, message = "La duracion minima es 15 minutos") @Max(value = 240, message = "La duracion maxima es 240 minutos") Integer duracionMinutos,
    Boolean activo) {}

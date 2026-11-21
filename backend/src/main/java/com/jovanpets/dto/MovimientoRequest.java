package com.jovanpets.dto;

import com.jovanpets.domain.TipoMovimiento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** HU-14: entrada o salida de stock. Si no se envia fecha se usa la fecha actual. */
public record MovimientoRequest(
    @NotBlank(message = "Selecciona el producto") String productoId,
    @NotNull(message = "Selecciona el tipo de movimiento") TipoMovimiento tipo,
    @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad debe ser mayor a cero") @Max(100000) Integer cantidad,
    @NotBlank(message = "El motivo es obligatorio") @Size(max = 300) String motivo,
    @PastOrPresent(message = "La fecha no puede ser futura") LocalDateTime fecha) {}

package com.jovanpets.dto;

import com.jovanpets.domain.TipoAtencion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** HU-06/HU-08: antecedente, control, vacuna o desparasitacion. */
public record AtencionRequest(
    @NotNull(message = "El tipo de atencion es obligatorio") TipoAtencion tipo,
    @NotNull(message = "La fecha es obligatoria") @PastOrPresent(message = "La fecha no puede ser futura") LocalDate fecha,
    @NotBlank(message = "El diagnostico o descripcion es obligatorio") @Size(max = 500) String descripcion,
    @Size(max = 2000) String observaciones,
    LocalDate proximaFecha) {}

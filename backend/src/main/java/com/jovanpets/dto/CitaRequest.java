package com.jovanpets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** HU-09: solicitud de cita. */
public record CitaRequest(
    @NotBlank(message = "Selecciona la mascota") String mascotaId,
    @NotBlank(message = "Selecciona el servicio") String servicioId,
    @NotNull(message = "Selecciona la fecha y hora") LocalDateTime inicio) {}

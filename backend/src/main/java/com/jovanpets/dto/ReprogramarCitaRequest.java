package com.jovanpets.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** RA-07: nueva fecha y hora para una cita. */
public record ReprogramarCitaRequest(@NotNull(message = "Selecciona la nueva fecha y hora") LocalDateTime inicio) {}

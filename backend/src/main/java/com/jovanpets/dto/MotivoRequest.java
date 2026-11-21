package com.jovanpets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivo obligatorio para rechazar o cancelar (HU-10, HU-23, RPE-05). */
public record MotivoRequest(@NotBlank(message = "El motivo es obligatorio") @Size(max = 500) String motivo) {}

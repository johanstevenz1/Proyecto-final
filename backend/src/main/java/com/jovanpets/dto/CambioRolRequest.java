package com.jovanpets.dto;

import com.jovanpets.domain.Rol;
import jakarta.validation.constraints.NotNull;

/** HU-20: cambio de rol. */
public record CambioRolRequest(@NotNull(message = "El rol es obligatorio") Rol rol) {}

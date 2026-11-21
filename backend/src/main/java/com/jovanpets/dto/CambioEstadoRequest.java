package com.jovanpets.dto;

import jakarta.validation.constraints.NotNull;

/** Activar o desactivar un registro. */
public record CambioEstadoRequest(@NotNull(message = "Indica si el registro queda activo") Boolean activo) {}

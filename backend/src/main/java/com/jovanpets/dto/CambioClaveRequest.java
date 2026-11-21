package com.jovanpets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** RU-05: cambio de contrasena desde el perfil. */
public record CambioClaveRequest(
    @NotBlank(message = "La contrasena actual es obligatoria") String claveActual,
    @NotBlank(message = "La contrasena nueva es obligatoria") @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres") String nuevaClave) {}

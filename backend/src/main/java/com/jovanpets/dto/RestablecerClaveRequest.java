package com.jovanpets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** HU-22: nueva contrasena con el token temporal recibido. */
public record RestablecerClaveRequest(
    @NotBlank(message = "El enlace no es valido") String token,
    @NotBlank(message = "La contrasena es obligatoria") @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres") String nuevaClave) {}

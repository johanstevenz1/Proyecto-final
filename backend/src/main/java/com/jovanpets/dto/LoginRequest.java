package com.jovanpets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** HU-19: inicio de sesion. */
public record LoginRequest(
    @NotBlank(message = "El email es obligatorio") @Email(message = "Email con formato no valido") String email,
    @NotBlank(message = "La contrasena es obligatoria") String clave) {}

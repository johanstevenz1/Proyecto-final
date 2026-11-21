package com.jovanpets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** HU-22: solicitud del enlace de recuperacion. */
public record RecuperarClaveRequest(
    @NotBlank(message = "El email es obligatorio") @Email(message = "Email con formato no valido") String email) {}

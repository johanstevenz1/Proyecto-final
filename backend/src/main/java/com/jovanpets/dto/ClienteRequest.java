package com.jovanpets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** HU-01/HU-02: datos personales del cliente. La cedula se ignora al editar. */
public record ClienteRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombre,
    @NotBlank(message = "La cedula es obligatoria") @Pattern(regexp = "\\d{5,15}", message = "La cedula debe tener entre 5 y 15 digitos") String cedula,
    @NotBlank(message = "El telefono es obligatorio") @Pattern(regexp = "[0-9+ ]{7,20}", message = "Telefono no valido") String telefono,
    @NotBlank(message = "El email es obligatorio") @Email(message = "Email con formato no valido") @Size(max = 120) String email,
    @NotBlank(message = "La direccion es obligatoria") @Size(max = 200) String direccion) {}

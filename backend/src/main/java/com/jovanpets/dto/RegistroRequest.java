package com.jovanpets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** HU-18: registro de usuario cliente con su ficha de propietario. */
public record RegistroRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombre,
    @NotBlank(message = "La cedula es obligatoria") @Pattern(regexp = "\\d{5,15}", message = "La cedula debe tener entre 5 y 15 digitos") String cedula,
    @NotBlank(message = "El telefono es obligatorio") @Pattern(regexp = "[0-9+ ]{7,20}", message = "Telefono no valido") String telefono,
    @NotBlank(message = "El email es obligatorio") @Email(message = "Email con formato no valido") @Size(max = 120) String email,
    @NotBlank(message = "La direccion es obligatoria") @Size(max = 200) String direccion,
    @NotBlank(message = "La contrasena es obligatoria") @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres") String clave) {}

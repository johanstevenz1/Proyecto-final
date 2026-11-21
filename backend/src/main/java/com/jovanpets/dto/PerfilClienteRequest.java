package com.jovanpets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos que el propio cliente puede actualizar desde su perfil. */
public record PerfilClienteRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombre,
    @NotBlank(message = "El telefono es obligatorio") @Pattern(regexp = "[0-9+ ]{7,20}", message = "Telefono no valido") String telefono,
    @NotBlank(message = "La direccion es obligatoria") @Size(max = 200) String direccion) {}

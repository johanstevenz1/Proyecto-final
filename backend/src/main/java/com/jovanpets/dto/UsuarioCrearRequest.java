package com.jovanpets.dto;

import com.jovanpets.domain.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Alta de una cuenta de personal (veterinario o administrador) por parte del administrador. */
public record UsuarioCrearRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombre,
    @NotBlank(message = "El email es obligatorio") @Email(message = "Email con formato no valido") String email,
    @NotBlank(message = "La contrasena es obligatoria") @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres") String clave,
    @NotNull(message = "El rol es obligatorio") Rol rol) {}

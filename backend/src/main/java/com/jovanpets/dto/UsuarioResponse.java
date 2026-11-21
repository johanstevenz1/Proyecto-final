package com.jovanpets.dto;

import com.jovanpets.domain.Rol;
import java.time.Instant;

/** Usuario expuesto por la API, sin hash de contrasena ni datos de recuperacion. */
public record UsuarioResponse(
    String id, String email, String nombre, Rol rol, boolean activo, String clienteId, Instant fechaRegistro) {}

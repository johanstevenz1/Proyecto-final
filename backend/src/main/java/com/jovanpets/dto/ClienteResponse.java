package com.jovanpets.dto;

import java.time.Instant;

public record ClienteResponse(
    String id, String nombre, String cedula, String telefono, String email, String direccion,
    boolean activo, Instant fechaRegistro) {}

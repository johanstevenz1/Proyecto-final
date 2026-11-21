package com.jovanpets.dto;

import java.time.Instant;

/** Token JWT emitido al iniciar sesion y datos del usuario autenticado. */
public record SesionResponse(String token, Instant expiraEn, UsuarioResponse usuario) {}

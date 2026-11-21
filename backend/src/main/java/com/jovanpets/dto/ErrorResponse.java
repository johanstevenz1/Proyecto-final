package com.jovanpets.dto;

import java.time.Instant;
import java.util.Map;

/** Formato unico de error de la API. */
public record ErrorResponse(Instant fecha, int estado, String mensaje, Map<String, String> errores) {

  public static ErrorResponse de(int estado, String mensaje) {
    return new ErrorResponse(Instant.now(), estado, mensaje, Map.of());
  }
}

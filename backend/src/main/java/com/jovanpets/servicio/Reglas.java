package com.jovanpets.servicio;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Reglas {
  private Reglas() {}

  public static void exigir(boolean condicion, String mensaje) {
    if (!condicion) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
  }

  public static String texto(String valor, String campo, int max) {
    exigir(valor != null && !valor.isBlank(), campo + " es obligatorio");
    String s = valor.trim();
    exigir(s.length() <= max, campo + " es demasiado largo");
    return s;
  }

  public static BigDecimal dinero(BigDecimal v) {
    exigir(
        v != null && v.signum() >= 0 && v.scale() <= 2 && v.precision() <= 14,
        "El precio debe ser positivo o cero y tener hasta dos decimales");
    return v;
  }

  public static <T> T existe(java.util.Optional<T> valor) {
    return valor.orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
  }

  public static void conflicto(boolean c, String mensaje) {
    if (c) throw new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
  }
}

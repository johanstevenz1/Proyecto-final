package com.jovanpets.exception;

import org.springframework.http.HttpStatus;

/** Violacion de una regla de negocio. Cada subclase define el codigo HTTP que corresponde. */
public class NegocioException extends RuntimeException {
  private final HttpStatus estado;

  public NegocioException(String mensaje) {
    this(HttpStatus.BAD_REQUEST, mensaje);
  }

  protected NegocioException(HttpStatus estado, String mensaje) {
    super(mensaje);
    this.estado = estado;
  }

  public HttpStatus getEstado() {
    return estado;
  }

  /** Lanza la excepcion si la condicion no se cumple. */
  public static void exigir(boolean condicion, String mensaje) {
    if (!condicion) {
      throw new NegocioException(mensaje);
    }
  }
}

package com.jovanpets.exception;

import org.springframework.http.HttpStatus;

/** Conflicto con el estado actual de los datos: duplicados, horarios ocupados o stock insuficiente. */
public class ConflictoException extends NegocioException {
  public ConflictoException(String mensaje) {
    super(HttpStatus.CONFLICT, mensaje);
  }
}

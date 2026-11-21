package com.jovanpets.exception;

import org.springframework.http.HttpStatus;

public class RecursoNoEncontradoException extends NegocioException {
  public RecursoNoEncontradoException(String recurso) {
    super(HttpStatus.NOT_FOUND, recurso + " no encontrado");
  }
}

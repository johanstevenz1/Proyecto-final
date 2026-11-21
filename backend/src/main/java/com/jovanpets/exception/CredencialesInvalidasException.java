package com.jovanpets.exception;

import org.springframework.http.HttpStatus;

public class CredencialesInvalidasException extends NegocioException {
  public CredencialesInvalidasException(String mensaje) {
    super(HttpStatus.UNAUTHORIZED, mensaje);
  }
}

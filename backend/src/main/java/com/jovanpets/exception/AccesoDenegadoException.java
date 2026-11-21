package com.jovanpets.exception;

import org.springframework.http.HttpStatus;

/** El usuario autenticado no puede acceder al recurso solicitado (HU-21). */
public class AccesoDenegadoException extends NegocioException {
  public AccesoDenegadoException() {
    super(HttpStatus.FORBIDDEN, "Acceso denegado");
  }
}

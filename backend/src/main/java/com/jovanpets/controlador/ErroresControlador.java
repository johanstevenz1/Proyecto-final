package com.jovanpets.controlador;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ErroresControlador {
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<?> regla(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(Map.of("mensaje", e.getReason() == null ? "Operacion no valida" : e.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> datos(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            Map.of(
                "mensaje",
                "Revisa los datos del formulario",
                "campos",
                e.getBindingResult().getFieldErrors().stream()
                    .map(x -> x.getField() + ": " + x.getDefaultMessage())
                    .toList()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> duplicado() {
    return ResponseEntity.status(409)
        .body(Map.of("mensaje", "Los datos ya existen o estan relacionados con otro registro"));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> formato() {
    return ResponseEntity.badRequest()
        .body(Map.of("mensaje", "Hay un valor o formato no valido en la solicitud"));
  }
}

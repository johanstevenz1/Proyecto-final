package com.jovanpets.exception;

import com.jovanpets.dto.ErrorResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Manejo global de excepciones con respuestas consistentes. */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(NegocioException.class)
  ResponseEntity<ErrorResponse> negocio(NegocioException e) {
    return respuesta(e.getEstado(), e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
    Map<String, String> errores = new LinkedHashMap<>();
    e.getBindingResult().getFieldErrors().forEach(f -> errores.putIfAbsent(f.getField(), f.getDefaultMessage()));
    e.getBindingResult().getGlobalErrors().forEach(g -> errores.putIfAbsent(g.getObjectName(), g.getDefaultMessage()));
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(Instant.now(), 400, "Revisa los datos ingresados", errores));
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  ResponseEntity<ErrorResponse> validacionParametros(HandlerMethodValidationException e) {
    return respuesta(HttpStatus.BAD_REQUEST, "Revisa los parametros enviados");
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  ResponseEntity<ErrorResponse> solicitudInvalida(Exception e) {
    return respuesta(HttpStatus.BAD_REQUEST, "Solicitud con formato o valores no validos");
  }

  @ExceptionHandler(DuplicateKeyException.class)
  ResponseEntity<ErrorResponse> duplicado(DuplicateKeyException e) {
    return respuesta(HttpStatus.CONFLICT, "El registro ya existe o el recurso ya esta ocupado");
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  ResponseEntity<ErrorResponse> concurrencia(OptimisticLockingFailureException e) {
    return respuesta(HttpStatus.CONFLICT, "Otro usuario modifico el registro; recarga e intenta de nuevo");
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ErrorResponse> accesoDenegado(AccessDeniedException e) {
    return respuesta(HttpStatus.FORBIDDEN, "Acceso denegado");
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<ErrorResponse> noAutenticado(AuthenticationException e) {
    return respuesta(HttpStatus.UNAUTHORIZED, "Inicia sesion para continuar");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ErrorResponse> metodo(HttpRequestMethodNotSupportedException e) {
    return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Metodo no permitido");
  }

  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<ErrorResponse> noExiste(NoResourceFoundException e) {
    return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado");
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> inesperado(Exception e) {
    log.error("Error no controlado", e);
    return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado");
  }

  private ResponseEntity<ErrorResponse> respuesta(HttpStatus estado, String mensaje) {
    return ResponseEntity.status(estado).body(ErrorResponse.de(estado.value(), mensaje));
  }
}

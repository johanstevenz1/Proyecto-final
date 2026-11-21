package com.jovanpets.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jovanpets.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Respuestas JSON para 401 (sin sesion valida) y 403 (acceso denegado, HU-21). */
@Component
public class ManejadorErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {
  private final ObjectMapper json;

  public ManejadorErroresSeguridad(ObjectMapper json) {
    this.json = json;
  }

  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e)
      throws IOException {
    escribir(res, 401, "Inicia sesion para continuar");
  }

  @Override
  public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException e)
      throws IOException {
    escribir(res, 403, "Acceso denegado");
  }

  private void escribir(HttpServletResponse res, int estado, String mensaje) throws IOException {
    res.setStatus(estado);
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    res.setCharacterEncoding("UTF-8");
    json.writeValue(res.getOutputStream(), ErrorResponse.de(estado, mensaje));
  }
}

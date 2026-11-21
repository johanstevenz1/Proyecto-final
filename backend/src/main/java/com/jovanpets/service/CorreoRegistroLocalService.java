package com.jovanpets.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementacion de desarrollo del {@link ServicioCorreo}: mientras no se elija un proveedor de
 * correo, el enlace de recuperacion se escribe en el registro del servidor. No informa al usuario
 * de un envio real; ver docs/DECISIONES.md.
 */
@Service
public class CorreoRegistroLocalService implements ServicioCorreo {
  private static final Logger log = LoggerFactory.getLogger(CorreoRegistroLocalService.class);

  @Override
  public void enviarRecuperacion(String destino, String nombre, String enlace) {
    log.warn("[CORREO PENDIENTE] Enlace de recuperacion para {}: {}", destino, enlace);
  }
}

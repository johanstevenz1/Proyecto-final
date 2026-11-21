package com.jovanpets.service;

/**
 * Puerto de salida hacia el servicio de correo (entidad externa de la vista de contexto). La
 * integracion con un proveedor real esta pendiente de seleccion.
 */
public interface ServicioCorreo {
  void enviarRecuperacion(String destino, String nombre, String enlace);
}

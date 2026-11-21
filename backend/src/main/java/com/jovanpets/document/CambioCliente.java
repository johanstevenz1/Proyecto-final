package com.jovanpets.document;

import java.time.Instant;

/** Entrada del historial de cambios de un cliente (HU-02). Se incrusta en {@link Cliente}. */
public class CambioCliente {
  private Instant fecha;
  private String usuario;
  private String detalle;

  public CambioCliente() {}

  public CambioCliente(Instant fecha, String usuario, String detalle) {
    this.fecha = fecha;
    this.usuario = usuario;
    this.detalle = detalle;
  }

  public Instant getFecha() { return fecha; }
  public void setFecha(Instant fecha) { this.fecha = fecha; }
  public String getUsuario() { return usuario; }
  public void setUsuario(String usuario) { this.usuario = usuario; }
  public String getDetalle() { return detalle; }
  public void setDetalle(String detalle) { this.detalle = detalle; }
}

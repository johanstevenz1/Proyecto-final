package com.jovanpets.document;

/**
 * Superclase de los registros que se dan de baja de forma logica (RC-05, RP-05, ROL-05): en lugar
 * de borrarse se desactivan, asi las referencias historicas (citas, pedidos, movimientos) siguen
 * siendo validas. MongoDB guarda el campo heredado {@code activo} en el documento de cada subclase.
 */
public abstract class RegistroActivable {
  private boolean activo = true;

  protected RegistroActivable() {}

  public boolean isActivo() { return activo; }
  public void setActivo(boolean activo) { this.activo = activo; }

  public void activar() { this.activo = true; }
  public void desactivar() { this.activo = false; }
}

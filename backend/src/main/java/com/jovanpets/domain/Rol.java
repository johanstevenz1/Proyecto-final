package com.jovanpets.domain;

/** Roles definidos en ROL-01 / HU-20. */
public enum Rol {
  CLIENTE,
  VETERINARIO,
  ADMINISTRADOR;

  public boolean esPersonal() {
    return this == VETERINARIO || this == ADMINISTRADOR;
  }
}

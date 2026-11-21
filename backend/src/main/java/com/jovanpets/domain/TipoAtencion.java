package com.jovanpets.domain;

/** Tipos de entrada del historial clinico (HU-06, HU-07, HU-08). */
public enum TipoAtencion {
  CONSULTA,
  CONTROL,
  CIRUGIA,
  VACUNA,
  DESPARASITACION,
  PELUQUERIA,
  OTRO;

  public boolean esPreventiva() {
    return this == VACUNA || this == DESPARASITACION;
  }
}

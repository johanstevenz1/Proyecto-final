package com.jovanpets.domain;

import java.util.EnumSet;
import java.util.Set;

/** Estados de una cita y transiciones permitidas. */
public enum EstadoCita {
  PENDIENTE,
  CONFIRMADA,
  RECHAZADA,
  CANCELADA,
  ATENDIDA;

  public Set<EstadoCita> siguientes() {
    return switch (this) {
      case PENDIENTE -> EnumSet.of(CONFIRMADA, RECHAZADA, CANCELADA);
      case CONFIRMADA -> EnumSet.of(ATENDIDA, CANCELADA);
      default -> EnumSet.noneOf(EstadoCita.class);
    };
  }

  public boolean puedeCambiarA(EstadoCita destino) {
    return siguientes().contains(destino);
  }

  /** Una cita activa ocupa su franja horaria en la agenda. */
  public boolean ocupaAgenda() {
    return this == PENDIENTE || this == CONFIRMADA || this == ATENDIDA;
  }

  public boolean admiteReprogramacion() {
    return this == PENDIENTE || this == CONFIRMADA;
  }
}

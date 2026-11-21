package com.jovanpets.domain;

import java.util.EnumSet;
import java.util.Set;

/** Estados de una cita y transiciones permitidas. */
public enum EstadoCita implements EstadoConTransiciones<EstadoCita> {
  PENDIENTE,
  CONFIRMADA,
  RECHAZADA,
  CANCELADA,
  ATENDIDA;

  @Override
  public Set<EstadoCita> siguientes() {
    return switch (this) {
      case PENDIENTE -> EnumSet.of(CONFIRMADA, RECHAZADA, CANCELADA);
      case CONFIRMADA -> EnumSet.of(ATENDIDA, CANCELADA);
      default -> EnumSet.noneOf(EstadoCita.class);
    };
  }


  /** Una cita activa ocupa su franja horaria en la agenda. */
  public boolean ocupaAgenda() {
    return this == PENDIENTE || this == CONFIRMADA || this == ATENDIDA;
  }

  public boolean admiteReprogramacion() {
    return this == PENDIENTE || this == CONFIRMADA;
  }
}

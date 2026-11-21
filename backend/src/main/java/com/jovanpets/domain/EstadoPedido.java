package com.jovanpets.domain;

import java.util.EnumSet;
import java.util.Set;

/** Estados de un pedido y transiciones permitidas. */
public enum EstadoPedido {
  PENDIENTE,
  CONFIRMADO,
  DESPACHADO,
  ENTREGADO,
  CANCELADO;

  public Set<EstadoPedido> siguientes() {
    return switch (this) {
      case PENDIENTE -> EnumSet.of(CONFIRMADO, CANCELADO);
      case CONFIRMADO -> EnumSet.of(DESPACHADO, CANCELADO);
      case DESPACHADO -> EnumSet.of(ENTREGADO);
      default -> EnumSet.noneOf(EstadoPedido.class);
    };
  }

  public boolean puedeCambiarA(EstadoPedido destino) {
    return siguientes().contains(destino);
  }

  /** RPE-05: solo se cancela antes de ser despachado. */
  public boolean esCancelable() {
    return puedeCambiarA(CANCELADO);
  }
}

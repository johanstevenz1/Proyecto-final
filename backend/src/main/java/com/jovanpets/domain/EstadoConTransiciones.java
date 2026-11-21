package com.jovanpets.domain;

import java.util.Set;

/**
 * Contrato de los estados con ciclo de vida (citas y pedidos): cada estado conoce a cuales puede
 * pasar. Los servicios validan las transiciones con este contrato antes de guardar.
 */
public interface EstadoConTransiciones<E extends Enum<E>> {

  /** Estados a los que se puede pasar desde el actual. */
  Set<E> siguientes();

  default boolean puedeCambiarA(E destino) {
    return siguientes().contains(destino);
  }
}

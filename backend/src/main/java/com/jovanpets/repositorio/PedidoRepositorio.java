package com.jovanpets.repositorio;

import com.jovanpets.dominio.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {
  java.util.List<Pedido> findByClienteIdOrderByFechaDesc(Long id);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select p from Pedido p where p.id=:id")
  java.util.Optional<Pedido> bloquear(
      @org.springframework.data.repository.query.Param("id") Long id);
}

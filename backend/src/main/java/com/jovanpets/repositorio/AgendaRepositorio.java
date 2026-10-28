package com.jovanpets.repositorio;

import com.jovanpets.dominio.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaRepositorio extends JpaRepository<Agenda, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select a from Agenda a where a.nombre=:nombre")
  java.util.Optional<Agenda> bloquear(
      @org.springframework.data.repository.query.Param("nombre") String nombre);
}

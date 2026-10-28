package com.jovanpets.repositorio;

import com.jovanpets.dominio.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaRepositorio extends JpaRepository<Cita, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select c from Cita c where c.id=:id")
  java.util.Optional<Cita> bloquear(@org.springframework.data.repository.query.Param("id") Long id);

  @org.springframework.data.jpa.repository.Query(
      "select c from Cita c where c.estado in ('PENDIENTE','CONFIRMADA') and c.inicio < :fin and"
          + " c.fin > :inicio and c.servicio.categoria = :categoria and (:ignorar is null or c.id"
          + " <> :ignorar)")
  java.util.List<Cita> solapadas(
      @org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
      @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin,
      @org.springframework.data.repository.query.Param("categoria") String categoria,
      @org.springframework.data.repository.query.Param("ignorar") Long ignorar);

  java.util.List<Cita> findByInicioBetweenOrderByInicio(
      java.time.LocalDateTime inicio, java.time.LocalDateTime fin);

  java.util.List<Cita> findByMascotaPropietarioIdOrderByInicioDesc(Long clienteId);
}

package com.jovanpets.repository;

import com.jovanpets.document.Cita;
import com.jovanpets.domain.CategoriaServicio;
import com.jovanpets.domain.EstadoCita;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CitaRepository extends MongoRepository<Cita, String> {
  List<Cita> findByClienteIdOrderByInicioDesc(String clienteId);

  List<Cita> findByInicioBetweenOrderByInicioAsc(LocalDateTime desde, LocalDateTime hasta);

  List<Cita> findByEstadoOrderByInicioAsc(EstadoCita estado);

  /** Citas que ocupan la agenda y se cruzan con el intervalo [desde, hasta). */
  List<Cita> findByAgendaAndBloqueaAgendaTrueAndInicioLessThanAndFinGreaterThan(
      CategoriaServicio agenda, LocalDateTime hasta, LocalDateTime desde);

  long countByEstado(EstadoCita estado);

  long countByEstadoAndInicioBetween(EstadoCita estado, LocalDateTime desde, LocalDateTime hasta);
}

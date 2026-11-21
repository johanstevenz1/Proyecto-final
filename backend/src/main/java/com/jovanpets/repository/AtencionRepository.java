package com.jovanpets.repository;

import com.jovanpets.document.Atencion;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AtencionRepository extends MongoRepository<Atencion, String> {
  /** HU-06/HU-07: orden cronologico inverso con rango de fechas inclusivo. */
  List<Atencion> findByMascotaIdAndFechaBetweenOrderByFechaDescFechaRegistroDesc(
      String mascotaId, LocalDate desde, LocalDate hasta);
}

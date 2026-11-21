package com.jovanpets.repository;

import com.jovanpets.document.Servicio;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServicioRepository extends MongoRepository<Servicio, String> {
  List<Servicio> findByActivoTrueOrderByNombreAsc();

  List<Servicio> findAllByOrderByNombreAsc();
}

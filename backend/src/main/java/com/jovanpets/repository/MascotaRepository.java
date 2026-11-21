package com.jovanpets.repository;

import com.jovanpets.document.Mascota;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MascotaRepository extends MongoRepository<Mascota, String> {
  List<Mascota> findByPropietarioIdAndActivoTrueOrderByNombreAsc(String propietarioId);

  List<Mascota> findByActivoTrueOrderByNombreAsc();

  long countByActivoTrue();

  long countByEspecieAndActivoTrue(com.jovanpets.domain.Especie especie);
}

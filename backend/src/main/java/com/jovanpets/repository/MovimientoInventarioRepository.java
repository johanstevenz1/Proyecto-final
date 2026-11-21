package com.jovanpets.repository;

import com.jovanpets.document.MovimientoInventario;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MovimientoInventarioRepository extends MongoRepository<MovimientoInventario, String> {
  List<MovimientoInventario> findByProductoIdOrderByFechaDesc(String productoId);

  List<MovimientoInventario> findTop100ByOrderByFechaDesc();
}

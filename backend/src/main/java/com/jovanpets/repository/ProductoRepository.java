package com.jovanpets.repository;

import com.jovanpets.document.Producto;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ProductoRepository extends MongoRepository<Producto, String>, ProductoStockOperations {

  /** HU-15: productos activos con stock menor al minimo configurado. */
  @Query(value = "{ 'activo': true, '$expr': { '$lt': ['$stock', '$stockMinimo'] } }", sort = "{ 'stock': 1 }")
  List<Producto> buscarStockBajo();

  List<Producto> findAllByOrderByNombreAsc();
}

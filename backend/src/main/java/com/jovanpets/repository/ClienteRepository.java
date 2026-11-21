package com.jovanpets.repository;

import com.jovanpets.document.Cliente;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ClienteRepository extends MongoRepository<Cliente, String> {
  boolean existsByCedula(String cedula);

  Optional<Cliente> findByCedula(String cedula);

  Page<Cliente> findByActivoTrue(Pageable pageable);

  long countByActivoTrue();

  /**
   * HU-03: nombre parcial (sin distinguir mayusculas), cedula exacta o telefono parcial. El texto
   * del patron llega ya escapado desde el servicio.
   */
  @Query(
      "{ 'activo': true, '$or': [ { 'nombre': { '$regex': ?0, '$options': 'i' } },"
          + " { 'cedula': ?1 }, { 'telefono': { '$regex': ?0 } } ] }")
  Page<Cliente> buscarActivos(String patron, String exacto, Pageable pageable);
}

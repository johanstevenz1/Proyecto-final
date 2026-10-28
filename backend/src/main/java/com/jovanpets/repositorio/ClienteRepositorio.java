package com.jovanpets.repositorio;

import com.jovanpets.dominio.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
  java.util.Optional<Cliente> findByCedula(String cedula);

  @org.springframework.data.jpa.repository.Query(
      "select c from Cliente c where lower(c.nombre) like lower(concat('%',:q,'%')) or c.cedula"
          + " like concat('%',:q,'%') or c.telefono like concat('%',:q,'%') order by c.nombre")
  java.util.List<Cliente> buscar(@org.springframework.data.repository.query.Param("q") String q);
}

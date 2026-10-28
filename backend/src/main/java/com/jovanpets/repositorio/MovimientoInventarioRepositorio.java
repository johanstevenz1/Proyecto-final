package com.jovanpets.repositorio;

import com.jovanpets.dominio.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioRepositorio extends JpaRepository<MovimientoInventario, Long> {
  java.util.List<MovimientoInventario> findAllByOrderByFechaDescIdDesc();
}

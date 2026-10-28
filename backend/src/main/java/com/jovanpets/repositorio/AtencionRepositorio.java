package com.jovanpets.repositorio;

import com.jovanpets.dominio.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtencionRepositorio extends JpaRepository<Atencion, Long> {
  java.util.List<Atencion> findByMascotaIdOrderByFechaDescIdDesc(Long id);
}

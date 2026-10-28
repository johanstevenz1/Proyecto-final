package com.jovanpets.repositorio;

import com.jovanpets.dominio.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MascotaRepositorio extends JpaRepository<Mascota, Long> {
  java.util.List<Mascota> findByPropietarioIdOrderByNombre(Long id);
}

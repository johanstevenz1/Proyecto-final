package com.jovanpets.repositorio;

import com.jovanpets.dominio.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepositorio extends JpaRepository<Servicio, Long> {}

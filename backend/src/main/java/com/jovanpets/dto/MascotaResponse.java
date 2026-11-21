package com.jovanpets.dto;

import com.jovanpets.domain.Especie;
import com.jovanpets.domain.Sexo;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MascotaResponse(
    String id, String propietarioId, String propietarioNombre, String nombre, Especie especie, String raza,
    Sexo sexo, LocalDate fechaNacimiento, BigDecimal peso, boolean activo) {}

package com.jovanpets.dto;

import com.jovanpets.domain.CategoriaServicio;
import com.jovanpets.domain.EstadoCita;
import java.time.LocalDateTime;

public record CitaResponse(
    String id, String mascotaId, String mascotaNombre, String clienteId, String clienteNombre,
    String servicioId, String servicioNombre, CategoriaServicio agenda, LocalDateTime inicio,
    LocalDateTime fin, EstadoCita estado, String motivo, String veterinarioId, String veterinarioNombre) {}

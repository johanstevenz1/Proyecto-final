package com.jovanpets.dto;

import com.jovanpets.domain.TipoAtencion;
import java.time.LocalDate;

public record AtencionResponse(
    String id, String mascotaId, TipoAtencion tipo, LocalDate fecha, String descripcion, String observaciones,
    String profesional, LocalDate proximaFecha) {}

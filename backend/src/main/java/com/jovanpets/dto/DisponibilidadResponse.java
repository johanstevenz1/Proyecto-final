package com.jovanpets.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** HU-09: horarios libres de un servicio en una fecha. */
public record DisponibilidadResponse(String servicioId, LocalDate fecha, List<LocalDateTime> horarios) {}

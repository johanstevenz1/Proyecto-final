package com.jovanpets.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Punto de una serie diaria para las graficas del dashboard. */
public record SerieDiaResponse(LocalDate fecha, long cantidad, BigDecimal valor) {}

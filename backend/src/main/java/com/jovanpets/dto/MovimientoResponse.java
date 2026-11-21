package com.jovanpets.dto;

import com.jovanpets.domain.TipoMovimiento;
import java.time.LocalDateTime;

public record MovimientoResponse(
    String id, String productoId, String productoNombre, LocalDateTime fecha, TipoMovimiento tipo, int cantidad,
    String motivo, int saldo, String usuarioNombre, String pedidoId) {}

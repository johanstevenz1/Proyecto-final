package com.jovanpets.dto;

import java.math.BigDecimal;
import java.util.List;

public record CotizacionResponse(List<DetallePedidoResponse> detalles, BigDecimal total) {}

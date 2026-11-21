package com.jovanpets.dto;

import java.math.BigDecimal;

public record DetallePedidoResponse(
    String productoId, String nombreProducto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}

package com.jovanpets.dto;

import com.jovanpets.domain.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoPedidoRequest(@NotNull(message = "El estado es obligatorio") EstadoPedido estado) {}

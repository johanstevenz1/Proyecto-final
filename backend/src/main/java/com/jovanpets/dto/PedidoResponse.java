package com.jovanpets.dto;

import com.jovanpets.domain.EstadoPedido;
import com.jovanpets.domain.ModalidadEntrega;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    String id, String numero, String clienteId, String clienteNombre, LocalDateTime fecha, EstadoPedido estado,
    ModalidadEntrega modalidad, String direccionEntrega, List<DetallePedidoResponse> detalles, BigDecimal total,
    String motivoCancelacion) {}

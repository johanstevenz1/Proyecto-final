package com.jovanpets.dto;

import com.jovanpets.domain.ModalidadEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** HU-17: confirmacion del pedido. clienteId solo lo usa el administrador. */
public record PedidoRequest(
    @NotEmpty(message = "El carrito esta vacio") @Size(max = 50) List<@Valid LineaPedidoRequest> lineas,
    @NotNull(message = "Selecciona la modalidad de entrega") ModalidadEntrega modalidad,
    @Size(max = 200) String direccionEntrega,
    String clienteId) {}

package com.jovanpets.dto;

import java.util.List;

/** Ficha del cliente con su historial de cambios y sus mascotas activas. */
public record ClienteDetalleResponse(
    ClienteResponse cliente, List<CambioClienteResponse> historial, List<MascotaResponse> mascotas) {}

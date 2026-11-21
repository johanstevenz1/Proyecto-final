package com.jovanpets.dto;

import com.jovanpets.domain.EstadoCita;
import java.util.List;
import java.util.Map;

/** HU-24: metricas y resumenes del dashboard. */
public record PanelResponse(
    long totalClientes, long totalMascotas, Map<EstadoCita, Long> citasHoy, long citasPendientes,
    List<ProductoResponse> alertasStock, long pedidosPendientes, List<PedidoResponse> pedidosRecientes) {}

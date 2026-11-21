package com.jovanpets.dto;

import com.jovanpets.domain.Especie;
import com.jovanpets.domain.EstadoCita;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** HU-24: metricas, resumenes y series para el dashboard. */
public record PanelResponse(
    long totalClientes,
    long totalMascotas,
    Map<EstadoCita, Long> citasHoy,
    long citasPendientes,
    List<ProductoResponse> alertasStock,
    long pedidosPendientes,
    List<PedidoResponse> pedidosRecientes,
    List<SerieDiaResponse> citasProximosDias,
    List<SerieDiaResponse> ventasUltimosDias,
    BigDecimal ventasMes,
    Map<Especie, Long> mascotasPorEspecie,
    List<ProductoVendidoResponse> productosMasVendidos) {}

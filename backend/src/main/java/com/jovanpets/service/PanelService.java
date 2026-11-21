package com.jovanpets.service;

import com.jovanpets.dto.PanelResponse;
import org.springframework.stereotype.Service;

/** Dashboard principal con metricas y resumenes (HU-24). */
@Service
public class PanelService {
  private final ClienteService clientes;
  private final MascotaService mascotas;
  private final CitaService citas;
  private final InventarioService inventario;
  private final PedidoService pedidos;

  public PanelService(
      ClienteService clientes,
      MascotaService mascotas,
      CitaService citas,
      InventarioService inventario,
      PedidoService pedidos) {
    this.clientes = clientes;
    this.mascotas = mascotas;
    this.citas = citas;
    this.inventario = inventario;
    this.pedidos = pedidos;
  }

  public PanelResponse resumen() {
    return new PanelResponse(
        clientes.contarActivos(),
        mascotas.contarActivas(),
        citas.resumenDelDia(),
        citas.pendientes(),
        inventario.alertas(),
        pedidos.contarPendientes(),
        pedidos.recientes());
  }
}

package com.jovanpets.service;

import com.jovanpets.document.Cita;
import com.jovanpets.document.DetallePedido;
import com.jovanpets.document.Pedido;
import com.jovanpets.domain.Especie;
import com.jovanpets.domain.EstadoPedido;
import com.jovanpets.dto.PanelResponse;
import com.jovanpets.dto.ProductoVendidoResponse;
import com.jovanpets.dto.SerieDiaResponse;
import com.jovanpets.repository.CitaRepository;
import com.jovanpets.repository.MascotaRepository;
import com.jovanpets.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Dashboard principal con metricas, resumenes y series para graficas (HU-24). */
@Service
public class PanelService {
  private static final int DIAS = 7;

  private final ClienteService clientes;
  private final MascotaService mascotas;
  private final CitaService citas;
  private final InventarioService inventario;
  private final PedidoService pedidos;
  private final PedidoRepository pedidoRepository;
  private final CitaRepository citaRepository;
  private final MascotaRepository mascotaRepository;
  private final Clock reloj;

  public PanelService(
      ClienteService clientes,
      MascotaService mascotas,
      CitaService citas,
      InventarioService inventario,
      PedidoService pedidos,
      PedidoRepository pedidoRepository,
      CitaRepository citaRepository,
      MascotaRepository mascotaRepository,
      Clock reloj) {
    this.clientes = clientes;
    this.mascotas = mascotas;
    this.citas = citas;
    this.inventario = inventario;
    this.pedidos = pedidos;
    this.pedidoRepository = pedidoRepository;
    this.citaRepository = citaRepository;
    this.mascotaRepository = mascotaRepository;
    this.reloj = reloj;
  }

  public PanelResponse resumen() {
    LocalDate hoy = LocalDate.now(reloj);
    List<Pedido> ultimos = pedidosVigentes(hoy.minusDays(DIAS - 1L), hoy);
    List<Pedido> delMes = pedidosVigentes(hoy.withDayOfMonth(1), hoy);
    return new PanelResponse(
        clientes.contarActivos(),
        mascotas.contarActivas(),
        citas.resumenDelDia(),
        citas.pendientes(),
        inventario.alertas(),
        pedidos.contarPendientes(),
        pedidos.recientes(),
        citasProximosDias(hoy),
        ventasPorDia(ultimos, hoy),
        total(delMes),
        mascotasPorEspecie(),
        masVendidos(delMes));
  }

  /** Citas activas (pendientes, confirmadas o atendidas) de hoy y los proximos 6 dias. */
  private List<SerieDiaResponse> citasProximosDias(LocalDate hoy) {
    List<Cita> lista =
        citaRepository.findByInicioBetweenOrderByInicioAsc(
            hoy.atStartOfDay().minusNanos(1_000_000), hoy.plusDays(DIAS).atStartOfDay());
    Map<LocalDate, Long> conteo = new HashMap<>();
    for (Cita c : lista) {
      if (c.getEstado().ocupaAgenda()) {
        conteo.merge(c.getInicio().toLocalDate(), 1L, Long::sum);
      }
    }
    List<SerieDiaResponse> serie = new ArrayList<>();
    for (int i = 0; i < DIAS; i++) {
      LocalDate dia = hoy.plusDays(i);
      serie.add(new SerieDiaResponse(dia, conteo.getOrDefault(dia, 0L), null));
    }
    return serie;
  }

  /** Pedidos no cancelados y su valor por dia en los ultimos 7 dias. */
  private List<SerieDiaResponse> ventasPorDia(List<Pedido> lista, LocalDate hoy) {
    List<SerieDiaResponse> serie = new ArrayList<>();
    for (int i = DIAS - 1; i >= 0; i--) {
      LocalDate dia = hoy.minusDays(i);
      List<Pedido> delDia = lista.stream().filter(p -> p.getFecha().toLocalDate().equals(dia)).toList();
      serie.add(new SerieDiaResponse(dia, delDia.size(), total(delDia)));
    }
    return serie;
  }

  private Map<Especie, Long> mascotasPorEspecie() {
    Map<Especie, Long> mapa = new EnumMap<>(Especie.class);
    for (Especie e : Especie.values()) {
      mapa.put(e, mascotaRepository.countByEspecieAndActivoTrue(e));
    }
    return mapa;
  }

  private List<ProductoVendidoResponse> masVendidos(List<Pedido> lista) {
    Map<String, Long> unidades = new HashMap<>();
    for (Pedido p : lista) {
      for (DetallePedido d : p.getDetalles()) {
        unidades.merge(d.getNombreProducto(), (long) d.getCantidad(), Long::sum);
      }
    }
    return unidades.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(5)
        .map(e -> new ProductoVendidoResponse(e.getKey(), e.getValue()))
        .toList();
  }

  private List<Pedido> pedidosVigentes(LocalDate desde, LocalDate hasta) {
    return pedidoRepository
        .findByFechaBetween(desde.atStartOfDay().minusNanos(1_000_000), hasta.plusDays(1).atStartOfDay())
        .stream()
        .filter(p -> p.getEstado() != EstadoPedido.CANCELADO)
        .toList();
  }

  private static BigDecimal total(List<Pedido> lista) {
    return lista.stream().map(Pedido::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}

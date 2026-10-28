package com.jovanpets.servicio;

import com.jovanpets.repositorio.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class PanelServicio implements IPanel {
  private final Acceso acceso;
  private final ClienteRepositorio clientes;
  private final MascotaRepositorio mascotas;
  private final ICitas citas;
  private final IVentas ventas;

  public PanelServicio(Acceso a, ClienteRepositorio c, MascotaRepositorio m, ICitas ci, IVentas v) {
    acceso = a;
    clientes = c;
    mascotas = m;
    citas = ci;
    ventas = v;
  }

  public Map<String, Object> resumen() {
    acceso.personal();
    return Map.of(
        "clientes",
        clientes.count(),
        "mascotas",
        mascotas.count(),
        "citasHoy",
        citas.listar(LocalDate.now(ZoneId.of("America/Bogota"))),
        "pedidos",
        ventas.pedidos(),
        "alertas",
        ventas.alertas());
  }
}

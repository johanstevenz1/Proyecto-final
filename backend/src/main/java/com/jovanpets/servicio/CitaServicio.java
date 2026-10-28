package com.jovanpets.servicio;

import static com.jovanpets.servicio.Reglas.*;

import com.jovanpets.controlador.Solicitudes.*;
import com.jovanpets.dominio.*;
import com.jovanpets.repositorio.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CitaServicio implements ICitas {
  private final ServicioRepositorio servicios;
  private final CitaRepositorio citas;
  private final AgendaRepositorio agendas;
  private final ClienteMascotaServicio mascotas;
  private final Acceso acceso;

  public CitaServicio(
      ServicioRepositorio s,
      CitaRepositorio c,
      AgendaRepositorio g,
      ClienteMascotaServicio m,
      Acceso a) {
    servicios = s;
    citas = c;
    agendas = g;
    mascotas = m;
    acceso = a;
  }

  public List<Servicio> servicios() {
    return servicios.findAll().stream().filter(s -> s.activo).toList();
  }

  @Transactional
  public Servicio guardarServicio(Long id, Servicio e) {
    acceso.administrador();
    Servicio s = id == null ? new Servicio() : existe(servicios.findById(id));
    s.nombre = texto(e.nombre, "Nombre", 100);
    s.descripcion = e.descripcion;
    String categoria = texto(e.categoria, "Categoria", 30);
    exigir(Set.of("VETERINARIA", "PELUQUERIA").contains(categoria), "Categoria no valida");
    exigir(
        id == null || s.categoria.equals(categoria),
        "Para cambiar de agenda crea otro servicio y desactiva el anterior");
    s.categoria = categoria;
    s.precio = dinero(e.precio);
    exigir(
        e.duracionMinutos >= 15 && e.duracionMinutos <= 240,
        "La duracion debe estar entre 15 y 240 minutos");
    s.duracionMinutos = e.duracionMinutos;
    s.activo = e.activo;
    return servicios.save(s);
  }

  public List<Cita> listar(LocalDate fecha) {
    var u = acceso.actual();
    if (u.rol == Rol.CLIENTE)
      return citas.findByMascotaPropietarioIdOrderByInicioDesc(u.cliente.id).stream()
          .filter(c -> fecha == null || c.inicio.toLocalDate().equals(fecha))
          .toList();
    return fecha == null
        ? citas.findAll().stream().sorted(Comparator.comparing(c -> c.inicio)).toList()
        : citas.findByInicioBetweenOrderByInicio(fecha.atStartOfDay(), fecha.atTime(23, 59, 59));
  }

  private LocalDateTime ahora() {
    return LocalDateTime.now(ZoneId.of("America/Bogota"));
  }

  private boolean horario(LocalDateTime inicio, LocalDateTime fin) {
    if (!inicio.toLocalDate().equals(fin.toLocalDate())
        || inicio.getSecond() != 0
        || inicio.getNano() != 0
        || inicio.getMinute() % 15 != 0) return false;
    LocalTime a = inicio.toLocalTime(), b = fin.toLocalTime();
    return (!a.isBefore(LocalTime.of(8, 0)) && !b.isAfter(LocalTime.NOON))
        || (!a.isBefore(LocalTime.of(14, 0)) && !b.isAfter(LocalTime.of(18, 0)));
  }

  public List<LocalDateTime> disponibilidad(Long servicioId, LocalDate fecha) {
    acceso.actual();
    Servicio s = existe(servicios.findById(servicioId));
    exigir(s.activo, "Servicio inactivo");
    exigir(fecha != null, "Selecciona una fecha");
    // Consultar una sola vez evita una consulta SQL por cada franja horaria.
    List<Cita> ocupadas =
        citas.solapadas(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay(), s.categoria, null);
    List<LocalDateTime> libres = new ArrayList<>();
    for (int h = 8 * 60; h < 18 * 60; h += 15) {
      LocalDateTime i = fecha.atStartOfDay().plusMinutes(h), fin = i.plusMinutes(s.duracionMinutos);
      if (i.isAfter(ahora())
          && horario(i, fin)
          && ocupadas.stream().noneMatch(c -> c.inicio.isBefore(fin) && c.fin.isAfter(i)))
        libres.add(i);
    }
    return libres;
  }

  private Cita completar(Cita c, Reserva e, Long ignorar) {
    Mascota m = mascotas.mascota(e.mascotaId());
    exigir(m.activo && m.propietario.activo, "La mascota o su propietario estan inactivos");
    Servicio s = existe(servicios.findById(e.servicioId()));
    exigir(s.activo, "Servicio inactivo");
    existe(agendas.bloquear(s.categoria));
    LocalDateTime fin = e.inicio().plusMinutes(s.duracionMinutos);
    exigir(e.inicio().isAfter(ahora()), "La cita debe ser futura");
    exigir(horario(e.inicio(), fin), "Selecciona un horario disponible de la jornada");
    conflicto(
        !citas.solapadas(e.inicio(), fin, s.categoria, ignorar).isEmpty(),
        "El horario ya fue reservado; selecciona otro");
    c.mascota = m;
    c.servicio = s;
    c.inicio = e.inicio();
    c.fin = fin;
    return c;
  }

  @Transactional
  public Cita reservar(Reserva e) {
    acceso.comprador();
    return citas.save(completar(new Cita(), e, null));
  }

  @Transactional
  public Cita cambiar(Long id, CambioCita e) {
    Cita c = existe(citas.bloquear(id));
    existe(agendas.bloquear(c.servicio.categoria));
    acceso.propietario(c.mascota.propietario.id);
    exigir(
        c.estado == EstadoCita.PENDIENTE || c.estado == EstadoCita.CONFIRMADA,
        "La cita ya no admite cambios");
    if (e.estado() == EstadoCita.CANCELADA) {
      exigir(c.inicio.isAfter(ahora()), "Solo se pueden cancelar citas futuras");
      c.motivo = texto(e.motivo(), "Motivo", 1000);
    } else {
      acceso.personal();
      exigir(
          (c.estado == EstadoCita.PENDIENTE
                  && (e.estado() == EstadoCita.CONFIRMADA || e.estado() == EstadoCita.RECHAZADA))
              || (c.estado == EstadoCita.CONFIRMADA && e.estado() == EstadoCita.ATENDIDA),
          "Transicion de cita no permitida");
      if (e.estado() == EstadoCita.RECHAZADA) c.motivo = texto(e.motivo(), "Motivo", 1000);
    }
    c.estado = e.estado();
    return citas.save(c);
  }

  @Transactional
  public Cita reagendar(Long id, Reserva e) {
    acceso.personal();
    Cita c = existe(citas.bloquear(id));
    exigir(
        c.estado == EstadoCita.PENDIENTE || c.estado == EstadoCita.CONFIRMADA,
        "La cita no se puede reprogramar");
    exigir(
        c.servicio.id.equals(e.servicioId()) && c.mascota.id.equals(e.mascotaId()),
        "Al reprogramar conserva la mascota y el servicio");
    return citas.save(completar(c, e, id));
  }
}

package com.jovanpets.service;

import com.jovanpets.document.Cita;
import com.jovanpets.document.Cliente;
import com.jovanpets.document.Mascota;
import com.jovanpets.document.Servicio;
import com.jovanpets.domain.EstadoCita;
import com.jovanpets.domain.HorarioAtencion;
import com.jovanpets.dto.CitaRequest;
import com.jovanpets.dto.CitaResponse;
import com.jovanpets.dto.DisponibilidadResponse;
import com.jovanpets.exception.AccesoDenegadoException;
import com.jovanpets.exception.ConflictoException;
import com.jovanpets.exception.NegocioException;
import com.jovanpets.exception.RecursoNoEncontradoException;
import com.jovanpets.mapper.CitaMapper;
import com.jovanpets.repository.CitaRepository;
import com.jovanpets.security.UsuarioAutenticado;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * Agenda de citas veterinarias y de peluqueria (HU-09, HU-10, HU-11, HU-23, RA-07).
 *
 * <p>No se permiten citas en horarios ocupados: cada cita activa guarda las franjas que ocupa y
 * el indice unico parcial (agenda, franjas) de MongoDB rechaza de forma atomica cualquier
 * solapamiento, incluso entre solicitudes concurrentes.
 */
@Service
public class CitaService {
  private static final int MAX_DIAS_ANTICIPACION = 90;
  private static final int MAX_DIAS_CONSULTA = 31;

  private final CitaRepository citas;
  private final MascotaService mascotas;
  private final ClienteService clientes;
  private final ServicioCatalogoService servicios;
  private final HorarioAtencion horario;
  private final Clock reloj;

  public CitaService(
      CitaRepository citas,
      MascotaService mascotas,
      ClienteService clientes,
      ServicioCatalogoService servicios,
      HorarioAtencion horario,
      Clock reloj) {
    this.citas = citas;
    this.mascotas = mascotas;
    this.clientes = clientes;
    this.servicios = servicios;
    this.horario = horario;
    this.reloj = reloj;
  }

  /** HU-09: horarios libres de un servicio en la fecha indicada. */
  public DisponibilidadResponse disponibilidad(String servicioId, LocalDate fecha) {
    Servicio s = servicios.obtenerActivo(servicioId);
    NegocioException.exigir(fecha != null, "Selecciona una fecha");
    LocalDateTime ahora = LocalDateTime.now(reloj);
    if (fecha.isBefore(ahora.toLocalDate()) || fecha.isAfter(ahora.toLocalDate().plusDays(MAX_DIAS_ANTICIPACION))) {
      return new DisponibilidadResponse(servicioId, fecha, List.of());
    }
    // Una sola consulta por dia: citas que bloquean la agenda de la categoria.
    List<Cita> ocupadas =
        citas.findByAgendaAndBloqueaAgendaTrueAndInicioLessThanAndFinGreaterThan(
            s.getCategoria(), fecha.plusDays(1).atStartOfDay(), fecha.atStartOfDay());
    List<LocalDateTime> libres =
        horario.iniciosPosibles(fecha, s.getDuracionMinutos()).stream()
            .filter(i -> i.isAfter(ahora))
            .filter(
                i -> {
                  LocalDateTime fin = i.plusMinutes(s.getDuracionMinutos());
                  return ocupadas.stream().noneMatch(c -> c.getInicio().isBefore(fin) && c.getFin().isAfter(i));
                })
            .toList();
    return new DisponibilidadResponse(servicioId, fecha, libres);
  }

  /** HU-09: el cliente (o el administrador en su nombre) solicita la cita; queda PENDIENTE. */
  public CitaResponse solicitar(CitaRequest r, UsuarioAutenticado actor) {
    Mascota m = mascotas.obtenerActiva(r.mascotaId());
    mascotas.verificarAcceso(m, actor);
    Cliente propietario = clientes.obtenerActivo(m.getPropietarioId());
    Servicio s = servicios.obtenerActivo(r.servicioId());

    Cita c = new Cita();
    c.setMascotaId(m.getId());
    c.setMascotaNombre(m.getNombre());
    c.setClienteId(propietario.getId());
    c.setClienteNombre(propietario.getNombre());
    c.setServicioId(s.getId());
    c.setServicioNombre(s.getNombre());
    c.setAgenda(s.getCategoria());
    c.setFechaSolicitud(Instant.now(reloj));
    programar(c, r.inicio(), s.getDuracionMinutos());
    return CitaMapper.aResponse(guardarReservando(c));
  }

  /** Cliente: sus citas. Personal: calendario diario/semanal con filtros (HU-11). */
  public List<CitaResponse> listar(
      LocalDate desde, LocalDate hasta, EstadoCita estado, String veterinarioId, UsuarioAutenticado actor) {
    Stream<Cita> resultado;
    if (actor.esCliente()) {
      resultado =
          actor.clienteId() == null ? Stream.empty() : citas.findByClienteIdOrderByInicioDesc(actor.clienteId()).stream();
      if (desde != null) {
        resultado = resultado.filter(c -> !c.getInicio().toLocalDate().isBefore(desde));
      }
      if (hasta != null) {
        resultado = resultado.filter(c -> !c.getInicio().toLocalDate().isAfter(hasta));
      }
    } else if (desde == null && hasta == null && estado == EstadoCita.PENDIENTE) {
      resultado = citas.findByEstadoOrderByInicioAsc(EstadoCita.PENDIENTE).stream(); // HU-10
    } else {
      LocalDate inicio = desde == null ? LocalDate.now(reloj) : desde;
      LocalDate fin = hasta == null ? inicio.plusDays(6) : hasta;
      NegocioException.exigir(!fin.isBefore(inicio), "La fecha final debe ser posterior a la inicial");
      NegocioException.exigir(!fin.isAfter(inicio.plusDays(MAX_DIAS_CONSULTA)), "Consulta como maximo 31 dias");
      resultado =
          citas.findByInicioBetweenOrderByInicioAsc(inicio.atStartOfDay().minusNanos(1_000_000), fin.plusDays(1).atStartOfDay())
              .stream();
    }
    if (estado != null) {
      resultado = resultado.filter(c -> c.getEstado() == estado);
    }
    if (veterinarioId != null && !veterinarioId.isBlank()) {
      resultado = resultado.filter(c -> veterinarioId.equals(c.getVeterinarioId()));
    }
    return resultado.map(CitaMapper::aResponse).toList();
  }

  public CitaResponse obtener(String id, UsuarioAutenticado actor) {
    Cita c = buscar(id);
    verificarAcceso(c, actor);
    return CitaMapper.aResponse(c);
  }

  /** HU-10: confirmar; el profesional que confirma queda asignado a la cita. */
  public CitaResponse confirmar(String id, UsuarioAutenticado actor) {
    Cita c = buscar(id);
    transicion(c, EstadoCita.CONFIRMADA);
    c.setVeterinarioId(actor.id());
    c.setVeterinarioNombre(actor.nombre());
    return CitaMapper.aResponse(citas.save(c));
  }

  /** HU-10: rechazar con motivo; la franja queda libre. */
  public CitaResponse rechazar(String id, String motivo, UsuarioAutenticado actor) {
    Cita c = buscar(id);
    transicion(c, EstadoCita.RECHAZADA);
    c.setMotivo(motivo.trim());
    c.setVeterinarioId(actor.id());
    c.setVeterinarioNombre(actor.nombre());
    c.setBloqueaAgenda(false);
    return CitaMapper.aResponse(citas.save(c));
  }

  /** HU-23: solo citas futuras, con motivo; el horario vuelve a estar disponible. */
  public CitaResponse cancelar(String id, String motivo, UsuarioAutenticado actor) {
    Cita c = buscar(id);
    verificarAcceso(c, actor);
    NegocioException.exigir(c.getInicio().isAfter(LocalDateTime.now(reloj)), "Solo se pueden cancelar citas futuras");
    transicion(c, EstadoCita.CANCELADA);
    c.setMotivo(motivo.trim());
    c.setBloqueaAgenda(false);
    return CitaMapper.aResponse(citas.save(c));
  }

  public CitaResponse marcarAtendida(String id, UsuarioAutenticado actor) {
    Cita c = buscar(id);
    transicion(c, EstadoCita.ATENDIDA);
    if (c.getVeterinarioId() == null) {
      c.setVeterinarioId(actor.id());
      c.setVeterinarioNombre(actor.nombre());
    }
    return CitaMapper.aResponse(citas.save(c));
  }

  /**
   * RA-07: reprogramar conservando mascota y servicio. Si la reprograma el cliente, la cita vuelve
   * a PENDIENTE para que el personal la confirme de nuevo.
   */
  public CitaResponse reprogramar(String id, LocalDateTime nuevoInicio, UsuarioAutenticado actor) {
    Cita c = buscar(id);
    verificarAcceso(c, actor);
    NegocioException.exigir(c.getEstado().admiteReprogramacion(), "La cita ya no se puede reprogramar");
    Servicio s = servicios.obtenerActivo(c.getServicioId());
    programar(c, nuevoInicio, s.getDuracionMinutos());
    if (actor.esCliente()) {
      c.setEstado(EstadoCita.PENDIENTE);
      c.setVeterinarioId(null);
      c.setVeterinarioNombre(null);
    }
    return CitaMapper.aResponse(guardarReservando(c));
  }

  /** Conteo de citas del dia por estado para el panel (HU-24). */
  public Map<EstadoCita, Long> resumenDelDia() {
    LocalDate hoy = LocalDate.now(reloj);
    Map<EstadoCita, Long> resumen = new EnumMap<>(EstadoCita.class);
    for (EstadoCita e : List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.ATENDIDA)) {
      resumen.put(e, citas.countByEstadoAndInicioBetween(e, hoy.atStartOfDay().minusNanos(1_000_000), hoy.plusDays(1).atStartOfDay()));
    }
    return resumen;
  }

  public long pendientes() {
    return citas.countByEstado(EstadoCita.PENDIENTE);
  }

  private void programar(Cita c, LocalDateTime inicio, int duracionMinutos) {
    LocalDateTime ahora = LocalDateTime.now(reloj);
    NegocioException.exigir(inicio.isAfter(ahora), "La cita debe ser en una fecha y hora futura");
    NegocioException.exigir(
        !inicio.toLocalDate().isAfter(ahora.toLocalDate().plusDays(MAX_DIAS_ANTICIPACION)),
        "Solo se agenda con hasta 90 dias de anticipacion");
    LocalDateTime fin = inicio.plusMinutes(duracionMinutos);
    NegocioException.exigir(horario.admite(inicio, fin), "Selecciona un horario dentro de la jornada de atencion");
    c.setInicio(inicio);
    c.setFin(fin);
    c.setFranjas(horario.franjas(inicio, fin));
    c.setBloqueaAgenda(true);
  }

  private Cita guardarReservando(Cita c) {
    try {
      return citas.save(c);
    } catch (DuplicateKeyException e) {
      throw new ConflictoException("El horario ya fue reservado; selecciona otro");
    }
  }

  private void transicion(Cita c, EstadoCita destino) {
    NegocioException.exigir(
        c.getEstado().puedeCambiarA(destino),
        "No se puede pasar una cita " + c.getEstado() + " a " + destino);
    c.setEstado(destino);
  }

  private void verificarAcceso(Cita c, UsuarioAutenticado actor) {
    if (actor.esCliente() && !c.getClienteId().equals(actor.clienteId())) {
      throw new AccesoDenegadoException();
    }
  }

  private Cita buscar(String id) {
    return citas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cita"));
  }
}

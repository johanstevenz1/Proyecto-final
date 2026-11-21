package com.jovanpets.service;

import com.jovanpets.document.Atencion;
import com.jovanpets.document.Mascota;
import com.jovanpets.dto.AtencionRequest;
import com.jovanpets.dto.AtencionResponse;
import com.jovanpets.exception.NegocioException;
import com.jovanpets.exception.RecursoNoEncontradoException;
import com.jovanpets.mapper.AtencionMapper;
import com.jovanpets.repository.AtencionRepository;
import com.jovanpets.security.UsuarioAutenticado;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/** Historial clinico: antecedentes, controles, vacunas y desparasitaciones (HU-06 a HU-08). */
@Service
public class AtencionService {
  private static final LocalDate DESDE_MINIMO = LocalDate.of(1900, 1, 1);
  private static final LocalDate HASTA_MAXIMO = LocalDate.of(2999, 12, 31);

  private final AtencionRepository atenciones;
  private final MascotaService mascotas;
  private final Clock reloj;

  public AtencionService(AtencionRepository atenciones, MascotaService mascotas, Clock reloj) {
    this.atenciones = atenciones;
    this.mascotas = mascotas;
    this.reloj = reloj;
  }

  public AtencionResponse registrar(String mascotaId, AtencionRequest r, UsuarioAutenticado actor) {
    Mascota m = mascotas.obtenerActiva(mascotaId);
    Atencion a = new Atencion();
    a.setMascotaId(m.getId());
    a.setProfesional(actor.nombre());
    a.setRegistradoPor(actor.id());
    a.setFechaRegistro(Instant.now(reloj));
    copiar(r, a);
    return AtencionMapper.aResponse(atenciones.save(a));
  }

  public AtencionResponse actualizar(String id, AtencionRequest r, UsuarioAutenticado actor) {
    Atencion a = buscar(id);
    copiar(r, a);
    a.setProfesional(actor.nombre());
    return AtencionMapper.aResponse(atenciones.save(a));
  }

  public void eliminar(String id) {
    atenciones.delete(buscar(id));
  }

  /** HU-07: historial en orden cronologico inverso, con filtro opcional por rango de fechas. */
  public List<AtencionResponse> historial(String mascotaId, LocalDate desde, LocalDate hasta, UsuarioAutenticado actor) {
    Mascota m = mascotas.buscar(mascotaId);
    mascotas.verificarAcceso(m, actor);
    LocalDate inicio = desde == null ? DESDE_MINIMO : desde;
    LocalDate fin = hasta == null ? HASTA_MAXIMO : hasta;
    NegocioException.exigir(!inicio.isAfter(fin), "La fecha inicial debe ser anterior a la final");
    // Between es exclusivo en Spring Data MongoDB: se amplia un dia para incluir los extremos.
    return atenciones
        .findByMascotaIdAndFechaBetweenOrderByFechaDescFechaRegistroDesc(mascotaId, inicio.minusDays(1), fin.plusDays(1))
        .stream()
        .map(AtencionMapper::aResponse)
        .toList();
  }

  private Atencion buscar(String id) {
    return atenciones.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Atencion"));
  }

  private static void copiar(AtencionRequest r, Atencion a) {
    NegocioException.exigir(
        r.proximaFecha() == null || r.proximaFecha().isAfter(r.fecha()),
        "La proxima fecha debe ser posterior a la fecha de la atencion");
    a.setTipo(r.tipo());
    a.setFecha(r.fecha());
    a.setDescripcion(r.descripcion().trim());
    a.setObservaciones(r.observaciones() == null ? null : r.observaciones().trim());
    a.setProximaFecha(r.proximaFecha());
  }
}

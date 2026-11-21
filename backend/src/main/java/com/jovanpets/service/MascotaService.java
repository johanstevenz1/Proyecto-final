package com.jovanpets.service;

import com.jovanpets.document.Cliente;
import com.jovanpets.document.Mascota;
import com.jovanpets.dto.MascotaRequest;
import com.jovanpets.dto.MascotaResponse;
import com.jovanpets.exception.AccesoDenegadoException;
import com.jovanpets.exception.NegocioException;
import com.jovanpets.exception.RecursoNoEncontradoException;
import com.jovanpets.mapper.MascotaMapper;
import com.jovanpets.repository.MascotaRepository;
import com.jovanpets.security.UsuarioAutenticado;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Gestion de mascotas asociadas a un propietario (HU-05, RM-01, RM-02, RM-05). */
@Service
public class MascotaService {
  private final MascotaRepository mascotas;
  private final ClienteService clientes;
  private final Clock reloj;

  public MascotaService(MascotaRepository mascotas, ClienteService clientes, Clock reloj) {
    this.mascotas = mascotas;
    this.clientes = clientes;
    this.reloj = reloj;
  }

  /** El cliente registra sus mascotas; el personal debe indicar el propietario existente. */
  public MascotaResponse crear(MascotaRequest r, UsuarioAutenticado actor) {
    String propietarioId = actor.esCliente() ? actor.clienteId() : r.propietarioId();
    NegocioException.exigir(propietarioId != null && !propietarioId.isBlank(), "Selecciona el propietario");
    Cliente propietario = clientes.obtenerActivo(propietarioId);
    Mascota m = new Mascota();
    m.setPropietarioId(propietario.getId());
    m.setFechaRegistro(Instant.now(reloj));
    copiar(r, m);
    return MascotaMapper.aResponse(mascotas.save(m), propietario.getNombre());
  }

  public List<MascotaResponse> listar(String propietarioId, UsuarioAutenticado actor) {
    List<Mascota> lista;
    if (actor.esCliente()) {
      lista = actor.clienteId() == null ? List.of() : mascotas.findByPropietarioIdAndActivoTrueOrderByNombreAsc(actor.clienteId());
    } else if (propietarioId != null && !propietarioId.isBlank()) {
      lista = mascotas.findByPropietarioIdAndActivoTrueOrderByNombreAsc(propietarioId);
    } else {
      lista = mascotas.findByActivoTrueOrderByNombreAsc();
    }
    Map<String, String> nombres = clientes.nombres(lista.stream().map(Mascota::getPropietarioId).toList());
    return lista.stream().map(m -> MascotaMapper.aResponse(m, nombres.get(m.getPropietarioId()))).toList();
  }

  public MascotaResponse obtener(String id, UsuarioAutenticado actor) {
    Mascota m = buscar(id);
    verificarAcceso(m, actor);
    return aResponse(m);
  }

  public MascotaResponse actualizar(String id, MascotaRequest r, UsuarioAutenticado actor) {
    Mascota m = obtenerActiva(id);
    verificarAcceso(m, actor);
    if (!actor.esCliente() && r.propietarioId() != null && !r.propietarioId().isBlank()
        && !r.propietarioId().equals(m.getPropietarioId())) {
      m.setPropietarioId(clientes.obtenerActivo(r.propietarioId()).getId());
    }
    copiar(r, m);
    return aResponse(mascotas.save(m));
  }

  public void desactivar(String id, UsuarioAutenticado actor) {
    Mascota m = buscar(id);
    verificarAcceso(m, actor);
    m.setActivo(false);
    mascotas.save(m);
  }

  /** HU-21: un cliente solo accede a sus propias mascotas. */
  public void verificarAcceso(Mascota m, UsuarioAutenticado actor) {
    if (actor.esCliente() && !m.getPropietarioId().equals(actor.clienteId())) {
      throw new AccesoDenegadoException();
    }
  }

  public Mascota obtenerActiva(String id) {
    Mascota m = buscar(id);
    NegocioException.exigir(m.isActivo(), "La mascota esta inactiva");
    return m;
  }

  public Mascota buscar(String id) {
    return mascotas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Mascota"));
  }

  public long contarActivas() {
    return mascotas.countByActivoTrue();
  }

  private MascotaResponse aResponse(Mascota m) {
    return MascotaMapper.aResponse(m, clientes.nombres(List.of(m.getPropietarioId())).get(m.getPropietarioId()));
  }

  private static void copiar(MascotaRequest r, Mascota m) {
    m.setNombre(r.nombre().trim());
    m.setEspecie(r.especie());
    m.setRaza(r.raza().trim());
    m.setSexo(r.sexo());
    m.setFechaNacimiento(r.fechaNacimiento());
    m.setPeso(r.peso());
  }
}

package com.jovanpets.service;

import com.jovanpets.document.Servicio;
import com.jovanpets.dto.ServicioRequest;
import com.jovanpets.dto.ServicioResponse;
import com.jovanpets.exception.NegocioException;
import com.jovanpets.exception.RecursoNoEncontradoException;
import com.jovanpets.mapper.ServicioMapper;
import com.jovanpets.repository.ServicioRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/** Catalogo de servicios veterinarios y de peluqueria (HU-12, RS-01 a RS-04). */
@Service
public class ServicioCatalogoService {
  private final ServicioRepository servicios;

  public ServicioCatalogoService(ServicioRepository servicios) {
    this.servicios = servicios;
  }

  /** Servicios visibles para los clientes al agendar. */
  public List<ServicioResponse> activos() {
    return servicios.findByActivoTrueOrderByNombreAsc().stream().map(ServicioMapper::aResponse).toList();
  }

  public List<ServicioResponse> todos() {
    return servicios.findAllByOrderByNombreAsc().stream().map(ServicioMapper::aResponse).toList();
  }

  public ServicioResponse crear(ServicioRequest r) {
    Servicio s = new Servicio();
    copiar(r, s);
    s.setActivo(r.activo() == null || r.activo());
    return ServicioMapper.aResponse(servicios.save(s));
  }

  public ServicioResponse actualizar(String id, ServicioRequest r) {
    Servicio s = buscar(id);
    copiar(r, s);
    if (r.activo() != null) {
      s.setActivo(r.activo());
    }
    return ServicioMapper.aResponse(servicios.save(s));
  }

  public ServicioResponse cambiarEstado(String id, boolean activo) {
    Servicio s = buscar(id);
    s.setActivo(activo);
    return ServicioMapper.aResponse(servicios.save(s));
  }

  public Servicio obtenerActivo(String id) {
    Servicio s = buscar(id);
    NegocioException.exigir(s.isActivo(), "El servicio no esta disponible");
    return s;
  }

  private Servicio buscar(String id) {
    return servicios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Servicio"));
  }

  private static void copiar(ServicioRequest r, Servicio s) {
    s.setNombre(r.nombre().trim());
    s.setDescripcion(r.descripcion().trim());
    s.setCategoria(r.categoria());
    s.setPrecio(r.precio());
    s.setDuracionMinutos(r.duracionMinutos());
  }
}

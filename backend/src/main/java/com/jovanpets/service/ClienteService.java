package com.jovanpets.service;

import com.jovanpets.document.CambioCliente;
import com.jovanpets.document.Cliente;
import com.jovanpets.dto.ClienteDetalleResponse;
import com.jovanpets.dto.ClienteRequest;
import com.jovanpets.dto.ClienteResponse;
import com.jovanpets.dto.MascotaResponse;
import com.jovanpets.dto.PaginaResponse;
import com.jovanpets.dto.PerfilClienteRequest;
import com.jovanpets.exception.AccesoDenegadoException;
import com.jovanpets.exception.ConflictoException;
import com.jovanpets.exception.NegocioException;
import com.jovanpets.exception.RecursoNoEncontradoException;
import com.jovanpets.mapper.ClienteMapper;
import com.jovanpets.mapper.MascotaMapper;
import com.jovanpets.repository.ClienteRepository;
import com.jovanpets.repository.MascotaRepository;
import com.jovanpets.repository.UsuarioRepository;
import com.jovanpets.security.UsuarioAutenticado;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/** Gestion de clientes (HU-01 a HU-04, RC-05). */
@Service
public class ClienteService {
  private final ClienteRepository clientes;
  private final MascotaRepository mascotas;
  private final UsuarioRepository usuarios;
  private final Clock reloj;

  public ClienteService(
      ClienteRepository clientes, MascotaRepository mascotas, UsuarioRepository usuarios, Clock reloj) {
    this.clientes = clientes;
    this.mascotas = mascotas;
    this.usuarios = usuarios;
    this.reloj = reloj;
  }

  /** HU-01: la cedula es unica (validacion previa e indice unico en MongoDB). */
  public ClienteResponse crear(ClienteRequest r, UsuarioAutenticado actor) {
    String cedula = r.cedula().trim();
    if (clientes.existsByCedula(cedula)) {
      throw new ConflictoException("Ya existe un cliente con la cedula " + cedula);
    }
    Cliente c = new Cliente();
    c.setCedula(cedula);
    c.setNombre(r.nombre().trim());
    c.setTelefono(r.telefono().trim());
    c.setEmail(AuthService.normalizarEmail(r.email()));
    c.setDireccion(r.direccion().trim());
    c.setFechaRegistro(Instant.now(reloj));
    c.getHistorial().add(new CambioCliente(Instant.now(reloj), actor.email(), "Registro del cliente"));
    return ClienteMapper.aResponse(clientes.save(c));
  }

  /** HU-02: todos los campos son editables excepto la cedula; cada cambio queda en el historial. */
  public ClienteResponse actualizar(String id, ClienteRequest r, UsuarioAutenticado actor) {
    Cliente c = buscar(id);
    NegocioException.exigir(c.getCedula().equals(r.cedula().trim()), "La cedula no se puede modificar");
    List<String> cambios = new ArrayList<>();
    cambiar(cambios, "nombre", c.getNombre(), r.nombre().trim(), c::setNombre);
    cambiar(cambios, "telefono", c.getTelefono(), r.telefono().trim(), c::setTelefono);
    cambiar(cambios, "email", c.getEmail(), AuthService.normalizarEmail(r.email()), c::setEmail);
    cambiar(cambios, "direccion", c.getDireccion(), r.direccion().trim(), c::setDireccion);
    return ClienteMapper.aResponse(guardarCambios(c, cambios, actor));
  }

  /** El cliente actualiza sus propios datos de contacto desde el portal. */
  public ClienteResponse actualizarPerfil(PerfilClienteRequest r, UsuarioAutenticado actor) {
    Cliente c = buscar(fichaPropia(actor));
    List<String> cambios = new ArrayList<>();
    cambiar(cambios, "nombre", c.getNombre(), r.nombre().trim(), c::setNombre);
    cambiar(cambios, "telefono", c.getTelefono(), r.telefono().trim(), c::setTelefono);
    cambiar(cambios, "direccion", c.getDireccion(), r.direccion().trim(), c::setDireccion);
    Cliente guardado = guardarCambios(c, cambios, actor);
    usuarios
        .findById(actor.id())
        .ifPresent(
            u -> {
              u.setNombre(guardado.getNombre());
              usuarios.save(u);
            });
    return ClienteMapper.aResponse(guardado);
  }

  /** HU-03/HU-04: busqueda paginada y ordenada por nombre o fecha de registro. */
  public PaginaResponse<ClienteResponse> buscar(String texto, int pagina, int tamano, String orden) {
    NegocioException.exigir(pagina >= 0 && tamano > 0 && tamano <= 100, "Paginacion no valida");
    Sort sort =
        "fechaRegistro".equals(orden)
            ? Sort.by(Sort.Direction.DESC, "fechaRegistro")
            : Sort.by(Sort.Direction.ASC, "nombre");
    PageRequest pageable = PageRequest.of(pagina, tamano, sort);
    Page<Cliente> resultado =
        texto == null || texto.isBlank()
            ? clientes.findByActivoTrue(pageable)
            : clientes.buscarActivos(Pattern.quote(texto.trim()), texto.trim(), pageable);
    return PaginaResponse.de(resultado.map(ClienteMapper::aResponse));
  }

  public ClienteDetalleResponse detalle(String id, UsuarioAutenticado actor) {
    if (actor.esCliente() && !id.equals(actor.clienteId())) {
      throw new AccesoDenegadoException();
    }
    Cliente c = buscar(id);
    List<MascotaResponse> suyas =
        mascotas.findByPropietarioIdAndActivoTrueOrderByNombreAsc(id).stream()
            .map(m -> MascotaMapper.aResponse(m, c.getNombre()))
            .toList();
    return ClienteMapper.aDetalle(c, suyas);
  }

  public ClienteDetalleResponse perfil(UsuarioAutenticado actor) {
    return detalle(fichaPropia(actor), actor);
  }

  /** RC-05: desactivacion logica; tambien desactiva la cuenta asociada. */
  public void desactivar(String id, UsuarioAutenticado actor) {
    Cliente c = buscar(id);
    if (!c.isActivo()) {
      return;
    }
    c.setActivo(false);
    guardarCambios(c, List.of("cliente desactivado"), actor);
    usuarios
        .findByClienteId(id)
        .ifPresent(
            u -> {
              u.setActivo(false);
              u.setVersionSesion(u.getVersionSesion() + 1);
              usuarios.save(u);
            });
  }

  /** Cliente existente y activo; lo usan otros servicios para validar referencias. */
  public Cliente obtenerActivo(String id) {
    Cliente c = buscar(id);
    NegocioException.exigir(c.isActivo(), "El cliente esta inactivo");
    return c;
  }

  public Map<String, String> nombres(Collection<String> ids) {
    return clientes.findAllById(ids.stream().filter(Objects::nonNull).distinct().toList()).stream()
        .collect(Collectors.toMap(Cliente::getId, Cliente::getNombre, (a, b) -> a));
  }

  public long contarActivos() {
    return clientes.countByActivoTrue();
  }

  private Cliente buscar(String id) {
    return clientes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cliente"));
  }

  private String fichaPropia(UsuarioAutenticado actor) {
    NegocioException.exigir(actor.clienteId() != null, "Tu cuenta no tiene ficha de cliente");
    return actor.clienteId();
  }

  private Cliente guardarCambios(Cliente c, List<String> cambios, UsuarioAutenticado actor) {
    if (cambios.isEmpty()) {
      return c;
    }
    c.getHistorial().add(new CambioCliente(Instant.now(reloj), actor.email(), String.join("; ", cambios)));
    return clientes.save(c);
  }

  private static void cambiar(
      List<String> cambios, String campo, String antes, String despues, Consumer<String> setter) {
    if (!Objects.equals(antes, despues)) {
      cambios.add(campo + ": '" + antes + "' -> '" + despues + "'");
      setter.accept(despues);
    }
  }
}

package com.jovanpets.service;

import com.jovanpets.document.Usuario;
import com.jovanpets.domain.Rol;
import com.jovanpets.dto.UsuarioCrearRequest;
import com.jovanpets.dto.UsuarioResponse;
import com.jovanpets.exception.ConflictoException;
import com.jovanpets.exception.NegocioException;
import com.jovanpets.exception.RecursoNoEncontradoException;
import com.jovanpets.mapper.UsuarioMapper;
import com.jovanpets.repository.UsuarioRepository;
import com.jovanpets.security.UsuarioAutenticado;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Administracion de cuentas y roles (HU-20, ROL-04, ROL-05). */
@Service
public class UsuarioService {
  private final UsuarioRepository usuarios;
  private final PasswordEncoder encoder;
  private final Clock reloj;

  public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder, Clock reloj) {
    this.usuarios = usuarios;
    this.encoder = encoder;
    this.reloj = reloj;
  }

  public List<UsuarioResponse> listar(Rol rol) {
    List<Usuario> lista = rol == null ? usuarios.findAllByOrderByNombreAsc() : usuarios.findByRolOrderByNombreAsc(rol);
    return lista.stream().map(UsuarioMapper::aResponse).toList();
  }

  /** Personal activo, usado para filtrar el calendario por veterinario (HU-11). */
  public List<UsuarioResponse> personal() {
    return usuarios.findByRolInAndActivoTrueOrderByNombreAsc(List.of(Rol.VETERINARIO, Rol.ADMINISTRADOR)).stream()
        .map(UsuarioMapper::aResponse)
        .toList();
  }

  /** Alta de cuentas de personal; los clientes se registran desde el portal. */
  public UsuarioResponse crearPersonal(UsuarioCrearRequest r) {
    NegocioException.exigir(r.rol().esPersonal(), "Desde aqui solo se crean cuentas de personal");
    String email = AuthService.normalizarEmail(r.email());
    if (usuarios.existsByEmail(email)) {
      throw new ConflictoException("El email ya esta registrado");
    }
    Usuario u = new Usuario();
    u.setNombre(r.nombre().trim());
    u.setEmail(email);
    u.setClaveHash(encoder.encode(r.clave()));
    u.setRol(r.rol());
    u.setFechaRegistro(Instant.now(reloj));
    return UsuarioMapper.aResponse(usuarios.save(u));
  }

  /** HU-20: el nuevo rol se aplica en la siguiente peticion del usuario. */
  public UsuarioResponse cambiarRol(String id, Rol rol, UsuarioAutenticado actor) {
    Usuario u = buscar(id);
    NegocioException.exigir(
        !(u.getId().equals(actor.id()) && rol != Rol.ADMINISTRADOR), "No puedes quitarte el rol de administrador");
    NegocioException.exigir(
        rol != Rol.CLIENTE || u.getClienteId() != null, "El usuario no tiene ficha de cliente para ese rol");
    u.setRol(rol);
    return UsuarioMapper.aResponse(usuarios.save(u));
  }

  /** ROL-05: desactivar invalida ademas las sesiones abiertas. */
  public UsuarioResponse cambiarEstado(String id, boolean activo, UsuarioAutenticado actor) {
    Usuario u = buscar(id);
    NegocioException.exigir(activo || !u.getId().equals(actor.id()), "No puedes desactivar tu propia cuenta");
    u.setActivo(activo);
    if (!activo) {
      u.setVersionSesion(u.getVersionSesion() + 1);
    }
    return UsuarioMapper.aResponse(usuarios.save(u));
  }

  private Usuario buscar(String id) {
    return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario"));
  }
}

package com.jovanpets.service;

import com.jovanpets.document.CambioCliente;
import com.jovanpets.document.Cliente;
import com.jovanpets.document.Usuario;
import com.jovanpets.domain.Rol;
import com.jovanpets.dto.CambioClaveRequest;
import com.jovanpets.dto.LoginRequest;
import com.jovanpets.dto.RegistroRequest;
import com.jovanpets.dto.RestablecerClaveRequest;
import com.jovanpets.dto.SesionResponse;
import com.jovanpets.dto.UsuarioResponse;
import com.jovanpets.exception.ConflictoException;
import com.jovanpets.exception.CredencialesInvalidasException;
import com.jovanpets.exception.NegocioException;
import com.jovanpets.exception.RecursoNoEncontradoException;
import com.jovanpets.mapper.UsuarioMapper;
import com.jovanpets.repository.ClienteRepository;
import com.jovanpets.repository.UsuarioRepository;
import com.jovanpets.security.TokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Registro, inicio y cierre de sesion, y recuperacion de contrasena (HU-18, HU-19, HU-22). */
@Service
public class AuthService {
  private static final Duration VIGENCIA_RECUPERACION = Duration.ofHours(24);
  private static final String CREDENCIALES_INCORRECTAS = "Correo o contrasena incorrectos";

  private final UsuarioRepository usuarios;
  private final ClienteRepository clientes;
  private final PasswordEncoder encoder;
  private final TokenService tokens;
  private final ServicioCorreo correo;
  private final Clock reloj;
  private final String frontendUrl;
  private final SecureRandom aleatorio = new SecureRandom();
  private final String hashFicticio;

  public AuthService(
      UsuarioRepository usuarios,
      ClienteRepository clientes,
      PasswordEncoder encoder,
      TokenService tokens,
      ServicioCorreo correo,
      Clock reloj,
      @Value("${app.frontend-url}") String frontendUrl) {
    this.usuarios = usuarios;
    this.clientes = clientes;
    this.encoder = encoder;
    this.tokens = tokens;
    this.correo = correo;
    this.reloj = reloj;
    this.frontendUrl = frontendUrl;
    this.hashFicticio = encoder.encode("valor-para-igualar-tiempos");
  }

  public static String normalizarEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  /** HU-18: crea la cuenta con rol CLIENTE y su ficha de propietario. */
  public SesionResponse registrar(RegistroRequest r) {
    String email = normalizarEmail(r.email());
    if (usuarios.existsByEmail(email)) {
      throw new ConflictoException("El email ya esta registrado");
    }
    String cedula = r.cedula().trim();
    Cliente cliente = clientes.findByCedula(cedula).orElse(null);
    boolean fichaNueva = cliente == null;
    if (!fichaNueva) {
      // Ficha creada antes por el administrador: solo se vincula si el correo coincide y no tiene cuenta.
      boolean vinculable =
          cliente.isActivo() && !usuarios.existsByClienteId(cliente.getId()) && email.equalsIgnoreCase(cliente.getEmail());
      if (!vinculable) {
        throw new ConflictoException("La cedula ya esta registrada");
      }
    } else {
      cliente = new Cliente();
      cliente.setNombre(r.nombre().trim());
      cliente.setCedula(cedula);
      cliente.setTelefono(r.telefono().trim());
      cliente.setEmail(email);
      cliente.setDireccion(r.direccion().trim());
      cliente.setFechaRegistro(Instant.now(reloj));
      cliente.getHistorial().add(new CambioCliente(Instant.now(reloj), email, "Registro desde el portal"));
      cliente = clientes.save(cliente);
    }

    Usuario u = new Usuario();
    u.setEmail(email);
    u.setNombre(cliente.getNombre());
    u.setClaveHash(encoder.encode(r.clave()));
    u.setRol(Rol.CLIENTE);
    u.setClienteId(cliente.getId());
    u.setFechaRegistro(Instant.now(reloj));
    try {
      u = usuarios.save(u);
    } catch (DuplicateKeyException e) {
      if (fichaNueva) {
        clientes.deleteById(cliente.getId()); // compensacion: no dejar una ficha huerfana
      }
      throw new ConflictoException("El email ya esta registrado");
    }
    return tokens.emitir(u);
  }

  /** HU-19: valida credenciales y emite el token. */
  public SesionResponse iniciarSesion(LoginRequest r) {
    Usuario u = usuarios.findByEmail(normalizarEmail(r.email())).orElse(null);
    if (u == null) {
      encoder.matches(r.clave(), hashFicticio); // tiempo de respuesta similar exista o no la cuenta
      throw new CredencialesInvalidasException(CREDENCIALES_INCORRECTAS);
    }
    if (!encoder.matches(r.clave(), u.getClaveHash())) {
      throw new CredencialesInvalidasException(CREDENCIALES_INCORRECTAS);
    }
    if (!u.isActivo()) {
      throw new CredencialesInvalidasException("La cuenta esta desactivada");
    }
    return tokens.emitir(u);
  }

  /** Renueva el token mientras el usuario esta activo (expiracion por inactividad, RNF-04). */
  public SesionResponse renovar(String usuarioId) {
    return tokens.emitir(buscar(usuarioId));
  }

  /** RU-03: invalida todos los tokens emitidos para el usuario. */
  public void cerrarSesion(String usuarioId) {
    Usuario u = buscar(usuarioId);
    u.setVersionSesion(u.getVersionSesion() + 1);
    usuarios.save(u);
  }

  public UsuarioResponse actual(String usuarioId) {
    return UsuarioMapper.aResponse(buscar(usuarioId));
  }

  /** RU-05: cambio de contrasena; cierra las demas sesiones y devuelve un token nuevo. */
  public SesionResponse cambiarClave(String usuarioId, CambioClaveRequest r) {
    Usuario u = buscar(usuarioId);
    if (!encoder.matches(r.claveActual(), u.getClaveHash())) {
      throw new NegocioException("La contrasena actual no es correcta");
    }
    u.setClaveHash(encoder.encode(r.nuevaClave()));
    u.setVersionSesion(u.getVersionSesion() + 1);
    return tokens.emitir(usuarios.save(u));
  }

  /** HU-22: genera un enlace temporal de 24 horas. La respuesta no revela si el correo existe. */
  public void solicitarRecuperacion(String email) {
    usuarios
        .findByEmail(normalizarEmail(email))
        .filter(Usuario::isActivo)
        .ifPresent(
            u -> {
              byte[] bytes = new byte[32];
              aleatorio.nextBytes(bytes);
              String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
              u.setRecuperacionHash(sha256(token));
              u.setRecuperacionExpira(Instant.now(reloj).plus(VIGENCIA_RECUPERACION));
              usuarios.save(u);
              correo.enviarRecuperacion(u.getEmail(), u.getNombre(), frontendUrl + "/restablecer?token=" + token);
            });
  }

  /** HU-22: aplica la nueva contrasena si el enlace es valido y no ha expirado. */
  public void restablecerClave(RestablecerClaveRequest r) {
    Usuario u =
        usuarios
            .findByRecuperacionHash(sha256(r.token()))
            .orElseThrow(() -> new NegocioException("El enlace no es valido o ya fue usado"));
    if (u.getRecuperacionExpira() == null || u.getRecuperacionExpira().isBefore(Instant.now(reloj))) {
      throw new NegocioException("El enlace expiro; solicita uno nuevo");
    }
    u.setClaveHash(encoder.encode(r.nuevaClave()));
    u.setRecuperacionHash(null);
    u.setRecuperacionExpira(null);
    u.setVersionSesion(u.getVersionSesion() + 1);
    usuarios.save(u);
  }

  private Usuario buscar(String usuarioId) {
    return usuarios.findById(usuarioId).orElseThrow(() -> new RecursoNoEncontradoException("Usuario"));
  }

  private static String sha256(String valor) {
    try {
      byte[] hash = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}

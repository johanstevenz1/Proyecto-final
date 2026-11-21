package com.jovanpets.controller;

import com.jovanpets.dto.CambioClaveRequest;
import com.jovanpets.dto.LoginRequest;
import com.jovanpets.dto.MensajeResponse;
import com.jovanpets.dto.RecuperarClaveRequest;
import com.jovanpets.dto.RegistroRequest;
import com.jovanpets.dto.RestablecerClaveRequest;
import com.jovanpets.dto.SesionResponse;
import com.jovanpets.dto.UsuarioResponse;
import com.jovanpets.security.UsuarioAutenticado;
import com.jovanpets.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Autenticacion: HU-18, HU-19, HU-22, RU-03 y RU-05. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  @PostMapping("/registro")
  @ResponseStatus(HttpStatus.CREATED)
  public SesionResponse registrar(@Valid @RequestBody RegistroRequest r) {
    return auth.registrar(r);
  }

  @PostMapping("/login")
  public SesionResponse login(@Valid @RequestBody LoginRequest r) {
    return auth.iniciarSesion(r);
  }

  @PostMapping("/renovar")
  public SesionResponse renovar(@AuthenticationPrincipal UsuarioAutenticado actor) {
    return auth.renovar(actor.id());
  }

  @PostMapping("/logout")
  public MensajeResponse logout(@AuthenticationPrincipal UsuarioAutenticado actor) {
    auth.cerrarSesion(actor.id());
    return new MensajeResponse("Sesion cerrada");
  }

  @GetMapping("/yo")
  public UsuarioResponse yo(@AuthenticationPrincipal UsuarioAutenticado actor) {
    return auth.actual(actor.id());
  }

  @PutMapping("/clave")
  public SesionResponse cambiarClave(
      @AuthenticationPrincipal UsuarioAutenticado actor, @Valid @RequestBody CambioClaveRequest r) {
    return auth.cambiarClave(actor.id(), r);
  }

  @PostMapping("/recuperar")
  public MensajeResponse recuperar(@Valid @RequestBody RecuperarClaveRequest r) {
    auth.solicitarRecuperacion(r.email());
    return new MensajeResponse("Si el correo esta registrado, recibira un enlace valido por 24 horas");
  }

  @PostMapping("/restablecer")
  public MensajeResponse restablecer(@Valid @RequestBody RestablecerClaveRequest r) {
    auth.restablecerClave(r);
    return new MensajeResponse("Contrasena actualizada; ya puedes iniciar sesion");
  }
}

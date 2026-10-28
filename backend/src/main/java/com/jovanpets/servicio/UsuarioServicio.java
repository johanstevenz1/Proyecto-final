package com.jovanpets.servicio;

import static com.jovanpets.servicio.Reglas.*;

import com.jovanpets.controlador.Solicitudes.*;
import com.jovanpets.dominio.*;
import com.jovanpets.repositorio.*;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServicio implements IUsuarios {
  private final UsuarioRepositorio usuarios;
  private final ClienteRepositorio clientes;
  private final PasswordEncoder claves;
  private final Acceso acceso;

  public UsuarioServicio(UsuarioRepositorio u, ClienteRepositorio c, PasswordEncoder p, Acceso a) {
    usuarios = u;
    clientes = c;
    claves = p;
    acceso = a;
  }

  @Transactional
  public Usuario registrar(Registro e) {
    conflicto(usuarios.existsByEmailIgnoreCase(e.email().trim()), "El correo ya esta registrado");
    conflicto(
        clientes.findByCedula(e.cedula().trim()).isPresent(),
        "La cedula ya existe; el personal debe revisar la ficha existente");
    Cliente c = new Cliente();
    c.nombre = e.nombre().trim();
    c.cedula = e.cedula().trim();
    c.telefono = e.telefono().trim();
    c.direccion = e.direccion().trim();
    exigir(
        e.clave().getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72,
        "La contrasena supera 72 bytes");
    c.email = e.email().trim().toLowerCase(java.util.Locale.ROOT);
    clientes.save(c);
    Usuario u = new Usuario();
    u.email = c.email;
    u.claveHash = claves.encode(e.clave());
    u.cliente = c;
    return usuarios.save(u);
  }

  public List<Usuario> listar() {
    acceso.administrador();
    return usuarios.findAll();
  }

  @Transactional
  public Usuario cambiar(Long id, CambioRol e) {
    acceso.administrador();
    Usuario u = existe(usuarios.findById(id));
    exigir(
        !u.id.equals(acceso.actual().id),
        "No puedes modificar tu propio rol ni desactivar tu cuenta");
    exigir(
        e.rol() != Rol.CLIENTE || u.cliente != null,
        "Esta cuenta necesita una ficha de cliente antes de asignar ese rol");
    u.rol = e.rol();
    u.activo = e.activo();
    return usuarios.save(u);
  }
}

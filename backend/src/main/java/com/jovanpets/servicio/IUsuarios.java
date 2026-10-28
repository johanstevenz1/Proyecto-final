package com.jovanpets.servicio;

import com.jovanpets.controlador.Solicitudes.*;
import com.jovanpets.dominio.*;
import java.util.List;

public interface IUsuarios {
  Usuario registrar(Registro entrada);

  List<Usuario> listar();

  Usuario cambiar(Long id, CambioRol entrada);
}

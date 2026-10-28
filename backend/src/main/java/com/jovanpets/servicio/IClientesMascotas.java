package com.jovanpets.servicio;

import com.jovanpets.dominio.*;
import java.util.List;

public interface IClientesMascotas {
  List<Cliente> clientes(String q);

  Cliente guardarCliente(Long id, Cliente entrada);

  List<Mascota> mascotas();

  Mascota guardarMascota(Long id, Mascota entrada);

  List<Atencion> historial(Long mascota);

  Atencion atender(Long mascota, Atencion entrada);
}

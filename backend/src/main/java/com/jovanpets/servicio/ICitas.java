package com.jovanpets.servicio;

import com.jovanpets.controlador.Solicitudes.*;
import com.jovanpets.dominio.*;
import java.time.*;
import java.util.List;

public interface ICitas {
  List<Servicio> servicios();

  Servicio guardarServicio(Long id, Servicio entrada);

  List<Cita> listar(LocalDate fecha);

  List<LocalDateTime> disponibilidad(Long servicio, LocalDate fecha);

  Cita reservar(Reserva entrada);

  Cita cambiar(Long id, CambioCita entrada);

  Cita reagendar(Long id, Reserva entrada);
}

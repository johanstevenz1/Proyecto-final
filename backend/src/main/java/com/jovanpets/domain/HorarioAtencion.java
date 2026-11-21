package com.jovanpets.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Regla de dominio de la agenda: jornadas de atencion e intervalos de reserva. Una cita ocupa
 * todas las franjas de {@code intervaloMinutos} que hay entre su inicio y su fin.
 */
public final class HorarioAtencion {

  public record Jornada(LocalTime inicio, LocalTime fin) {
    public Jornada {
      if (!inicio.isBefore(fin)) {
        throw new IllegalArgumentException("Jornada invalida: " + inicio + "-" + fin);
      }
    }
  }

  private final List<Jornada> jornadas;
  private final int intervaloMinutos;

  public HorarioAtencion(List<Jornada> jornadas, int intervaloMinutos) {
    if (jornadas.isEmpty() || intervaloMinutos <= 0) {
      throw new IllegalArgumentException("Configuracion de agenda invalida");
    }
    this.jornadas = List.copyOf(jornadas);
    this.intervaloMinutos = intervaloMinutos;
  }

  /** Interpreta un texto como "08:00-12:00,14:00-18:00". */
  public static HorarioAtencion desde(String texto, int intervaloMinutos) {
    List<Jornada> lista = new ArrayList<>();
    for (String parte : texto.split(",")) {
      String[] limites = parte.trim().split("-");
      lista.add(new Jornada(LocalTime.parse(limites[0].trim()), LocalTime.parse(limites[1].trim())));
    }
    return new HorarioAtencion(lista, intervaloMinutos);
  }

  public int intervaloMinutos() {
    return intervaloMinutos;
  }

  /** La cita debe empezar en un multiplo del intervalo y terminar dentro de la misma jornada. */
  public boolean admite(LocalDateTime inicio, LocalDateTime fin) {
    if (!inicio.toLocalDate().equals(fin.toLocalDate()) || !fin.isAfter(inicio)) {
      return false;
    }
    LocalTime a = inicio.toLocalTime();
    if (a.getSecond() != 0 || a.getNano() != 0 || (a.getHour() * 60 + a.getMinute()) % intervaloMinutos != 0) {
      return false;
    }
    LocalTime b = fin.toLocalTime();
    return jornadas.stream().anyMatch(j -> !a.isBefore(j.inicio()) && !b.isAfter(j.fin()));
  }

  /** Franjas que ocupa una cita; se usan para impedir solapamientos mediante un indice unico. */
  public List<LocalDateTime> franjas(LocalDateTime inicio, LocalDateTime fin) {
    List<LocalDateTime> resultado = new ArrayList<>();
    for (LocalDateTime t = inicio; t.isBefore(fin); t = t.plusMinutes(intervaloMinutos)) {
      resultado.add(t);
    }
    return resultado;
  }

  /** Posibles horas de inicio en una fecha para un servicio de la duracion indicada. */
  public List<LocalDateTime> iniciosPosibles(LocalDate fecha, int duracionMinutos) {
    List<LocalDateTime> resultado = new ArrayList<>();
    for (Jornada j : jornadas) {
      for (LocalTime t = j.inicio(); !t.plusMinutes(duracionMinutos).isAfter(j.fin()) && !t.isBefore(j.inicio());
          t = t.plusMinutes(intervaloMinutos)) {
        resultado.add(fecha.atTime(t));
        if (t.plusMinutes(intervaloMinutos).isBefore(t)) {
          break; // evita desbordar la medianoche
        }
      }
    }
    return resultado;
  }
}

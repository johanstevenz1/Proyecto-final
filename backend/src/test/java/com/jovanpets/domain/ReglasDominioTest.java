package com.jovanpets.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias de las reglas puras del dominio. */
class ReglasDominioTest {
  private final HorarioAtencion horario = HorarioAtencion.desde("08:00-12:00,14:00-18:00", 15);
  private final LocalDate dia = LocalDate.of(2030, 3, 4);

  @Test
  void admiteSoloCitasDentroDeLaJornadaYAlineadas() {
    assertThat(horario.admite(dia.atTime(8, 0), dia.atTime(8, 30))).isTrue();
    assertThat(horario.admite(dia.atTime(11, 30), dia.atTime(12, 0))).isTrue();
    assertThat(horario.admite(dia.atTime(11, 45), dia.atTime(12, 15))).as("cruza el almuerzo").isFalse();
    assertThat(horario.admite(dia.atTime(8, 10), dia.atTime(8, 40))).as("no alineada a 15 min").isFalse();
    assertThat(horario.admite(dia.atTime(7, 45), dia.atTime(8, 15))).as("antes de abrir").isFalse();
    assertThat(horario.admite(dia.atTime(17, 30), dia.atTime(18, 0))).isTrue();
  }

  @Test
  void calculaLasFranjasQueOcupaUnaCita() {
    assertThat(horario.franjas(dia.atTime(9, 0), dia.atTime(10, 0)))
        .containsExactly(dia.atTime(9, 0), dia.atTime(9, 15), dia.atTime(9, 30), dia.atTime(9, 45));
  }

  @Test
  void generaIniciosPosiblesSegunLaDuracion() {
    var inicios = horario.iniciosPosibles(dia, 60);
    assertThat(inicios).first().isEqualTo(dia.atTime(8, 0));
    assertThat(inicios).contains(dia.atTime(11, 0), dia.atTime(14, 0), dia.atTime(17, 0));
    assertThat(inicios).doesNotContain(dia.atTime(11, 15), dia.atTime(17, 15));
    assertThat(inicios).allMatch(i -> horario.admite(i, i.plusMinutes(60)));
  }

  @Test
  void transicionesDeCita() {
    assertThat(EstadoCita.PENDIENTE.puedeCambiarA(EstadoCita.CONFIRMADA)).isTrue();
    assertThat(EstadoCita.PENDIENTE.puedeCambiarA(EstadoCita.ATENDIDA)).isFalse();
    assertThat(EstadoCita.CONFIRMADA.puedeCambiarA(EstadoCita.CANCELADA)).isTrue();
    assertThat(EstadoCita.CANCELADA.siguientes()).isEmpty();
    assertThat(EstadoCita.RECHAZADA.ocupaAgenda()).isFalse();
  }

  @Test
  void transicionesDePedido() {
    assertThat(EstadoPedido.PENDIENTE.esCancelable()).isTrue();
    assertThat(EstadoPedido.CONFIRMADO.esCancelable()).isTrue();
    assertThat(EstadoPedido.DESPACHADO.esCancelable()).as("RPE-05").isFalse();
    assertThat(EstadoPedido.DESPACHADO.puedeCambiarA(EstadoPedido.ENTREGADO)).isTrue();
    assertThat(EstadoPedido.ENTREGADO.siguientes()).isEmpty();
  }

  @Test
  void rechazaUnaJornadaInvalida() {
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> HorarioAtencion.desde("12:00-08:00", 15))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(LocalDateTime.of(2030, 1, 1, 0, 0)).isNotNull();
  }
}

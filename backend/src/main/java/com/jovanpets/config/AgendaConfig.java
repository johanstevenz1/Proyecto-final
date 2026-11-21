package com.jovanpets.config;

import com.jovanpets.domain.HorarioAtencion;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj de la zona horaria del negocio y jornadas de la agenda (configurables). */
@Configuration
public class AgendaConfig {

  @Bean
  Clock reloj(@Value("${app.zona-horaria}") String zona) {
    return Clock.system(ZoneId.of(zona));
  }

  @Bean
  HorarioAtencion horarioAtencion(
      @Value("${app.agenda.jornadas}") String jornadas, @Value("${app.agenda.intervalo-minutos}") int intervalo) {
    return HorarioAtencion.desde(jornadas, intervalo);
  }
}

package com.jovanpets.dominio;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "cita")
public class Cita {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  public Mascota mascota;

  @ManyToOne(optional = false)
  public Servicio servicio;

  @Column(nullable = false)
  public LocalDateTime inicio;

  @Column(nullable = false)
  public LocalDateTime fin;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public EstadoCita estado = EstadoCita.PENDIENTE;

  @Column(length = 1000)
  public String motivo;
}

package com.jovanpets.dominio;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "atencion")
public class Atencion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  public Mascota mascota;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public TipoAtencion tipo;

  @Column(nullable = false)
  public LocalDate fecha;

  @Column(nullable = false, length = 4000)
  public String descripcion;

  public String profesional;
  public LocalDate proximaFecha;
}

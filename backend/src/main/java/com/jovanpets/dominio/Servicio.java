package com.jovanpets.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "servicio")
public class Servicio {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public String nombre;

  @Column(length = 2000)
  public String descripcion;

  @Column(nullable = false)
  public String categoria;

  @Column(nullable = false, precision = 14, scale = 2)
  public BigDecimal precio;

  public int duracionMinutos;
  public boolean activo = true;
}

package com.jovanpets.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "mascota")
public class Mascota {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public String nombre;

  @Column(nullable = false)
  public String especie;

  public String raza;
  public String sexo;
  public LocalDate fechaNacimiento;

  @Column(precision = 10, scale = 2)
  public BigDecimal peso;

  @ManyToOne(optional = false)
  public Cliente propietario;

  public boolean activo = true;
}

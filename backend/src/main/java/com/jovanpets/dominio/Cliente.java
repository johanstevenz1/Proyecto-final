package com.jovanpets.dominio;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "cliente")
public class Cliente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 120)
  public String nombre;

  @Column(unique = true, length = 30)
  public String cedula;

  public String telefono;
  public String email;
  public String direccion;
  public boolean activo = true;
}

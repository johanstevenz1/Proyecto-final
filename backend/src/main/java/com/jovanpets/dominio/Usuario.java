package com.jovanpets.dominio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "usuario")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true, length = 180)
  public String email;

  @JsonIgnore
  @Column(nullable = false)
  public String claveHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public Rol rol = Rol.CLIENTE;

  public boolean activo = true;

  @OneToOne
  @JoinColumn(unique = true)
  public Cliente cliente;
}

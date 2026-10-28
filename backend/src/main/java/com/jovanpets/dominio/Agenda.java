package com.jovanpets.dominio;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "agenda")
public class Agenda {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true)
  public String nombre;
}

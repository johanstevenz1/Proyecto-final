package com.jovanpets.dominio;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  public Producto producto;

  @Column(nullable = false)
  public LocalDateTime fecha;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public TipoMovimiento tipo;

  public int cantidad;

  @Column(nullable = false, length = 500)
  public String motivo;

  public int saldo;
}

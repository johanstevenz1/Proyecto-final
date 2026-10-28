package com.jovanpets.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "pedido")
public class Pedido {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  public Cliente cliente;

  @Column(nullable = false)
  public LocalDateTime fecha;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public EstadoPedido estado = EstadoPedido.PENDIENTE;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public ModalidadEntrega modalidad;

  public String direccionEntrega;

  @Column(nullable = false, precision = 14, scale = 2)
  public BigDecimal total = BigDecimal.ZERO;

  @OneToMany(
      mappedBy = "pedido",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  public java.util.List<DetallePedido> detalles = new java.util.ArrayList<>();
}

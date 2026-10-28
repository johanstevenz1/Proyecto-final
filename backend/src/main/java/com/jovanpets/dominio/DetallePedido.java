package com.jovanpets.dominio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @JsonIgnore
  @ManyToOne(optional = false)
  public Pedido pedido;

  @ManyToOne(optional = false)
  public Producto producto;

  @Column(nullable = false)
  public String nombreProducto;

  public int cantidad;

  @Column(nullable = false, precision = 14, scale = 2)
  public BigDecimal precioUnitario;

  @Column(nullable = false, precision = 14, scale = 2)
  public BigDecimal subtotal;
}

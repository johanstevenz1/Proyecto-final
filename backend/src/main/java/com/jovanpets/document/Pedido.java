package com.jovanpets.document;

import com.jovanpets.domain.EstadoPedido;
import com.jovanpets.domain.ModalidadEntrega;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/** Pedido de productos (HU-17). Los detalles se incrustan; el cliente se referencia por id. */
@Document("pedidos")
public class Pedido {
  @Id private String id;

  /** Bloqueo optimista ante modificaciones concurrentes. */
  @Version private Long version;

  @Indexed(unique = true)
  private String numero;

  @Indexed private String clienteId;
  private String clienteNombre;
  @Indexed private LocalDateTime fecha;
  private EstadoPedido estado = EstadoPedido.PENDIENTE;
  private ModalidadEntrega modalidad;
  private String direccionEntrega;
  private List<DetallePedido> detalles = new ArrayList<>();

  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal total;

  private String motivoCancelacion;

  public Long getVersion() { return version; }
  public void setVersion(Long version) { this.version = version; }
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getNumero() { return numero; }
  public void setNumero(String numero) { this.numero = numero; }
  public String getClienteId() { return clienteId; }
  public void setClienteId(String clienteId) { this.clienteId = clienteId; }
  public String getClienteNombre() { return clienteNombre; }
  public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
  public LocalDateTime getFecha() { return fecha; }
  public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
  public EstadoPedido getEstado() { return estado; }
  public void setEstado(EstadoPedido estado) { this.estado = estado; }
  public ModalidadEntrega getModalidad() { return modalidad; }
  public void setModalidad(ModalidadEntrega modalidad) { this.modalidad = modalidad; }
  public String getDireccionEntrega() { return direccionEntrega; }
  public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
  public List<DetallePedido> getDetalles() { return detalles; }
  public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
  public BigDecimal getTotal() { return total; }
  public void setTotal(BigDecimal total) { this.total = total; }
  public String getMotivoCancelacion() { return motivoCancelacion; }
  public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
}

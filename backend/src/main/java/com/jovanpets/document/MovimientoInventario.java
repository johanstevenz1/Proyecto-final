package com.jovanpets.document;

import com.jovanpets.domain.TipoMovimiento;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/** Entrada o salida de existencias (HU-14). Registro inmutable con el saldo resultante. */
@Document("movimientos_inventario")
@CompoundIndex(name = "producto_fecha", def = "{'productoId': 1, 'fecha': -1}")
public class MovimientoInventario {
  @Id private String id;
  private String productoId;
  private String productoNombre;
  private LocalDateTime fecha;
  private TipoMovimiento tipo;
  private int cantidad;
  private String motivo;
  private int saldo;
  private String usuarioId;
  private String usuarioNombre;
  private String pedidoId;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getProductoId() { return productoId; }
  public void setProductoId(String productoId) { this.productoId = productoId; }
  public String getProductoNombre() { return productoNombre; }
  public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
  public LocalDateTime getFecha() { return fecha; }
  public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
  public TipoMovimiento getTipo() { return tipo; }
  public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }
  public int getCantidad() { return cantidad; }
  public void setCantidad(int cantidad) { this.cantidad = cantidad; }
  public String getMotivo() { return motivo; }
  public void setMotivo(String motivo) { this.motivo = motivo; }
  public int getSaldo() { return saldo; }
  public void setSaldo(int saldo) { this.saldo = saldo; }
  public String getUsuarioId() { return usuarioId; }
  public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
  public String getUsuarioNombre() { return usuarioNombre; }
  public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
  public String getPedidoId() { return pedidoId; }
  public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }
}

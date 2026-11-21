package com.jovanpets.document;

import java.math.BigDecimal;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * Linea de un pedido. Se incrusta en {@link Pedido} (composicion) con el precio vigente al
 * momento de la compra.
 */
public class DetallePedido {
  private String productoId;
  private String nombreProducto;
  private int cantidad;

  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal precioUnitario;

  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal subtotal;

  public String getProductoId() { return productoId; }
  public void setProductoId(String productoId) { this.productoId = productoId; }
  public String getNombreProducto() { return nombreProducto; }
  public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
  public int getCantidad() { return cantidad; }
  public void setCantidad(int cantidad) { this.cantidad = cantidad; }
  public BigDecimal getPrecioUnitario() { return precioUnitario; }
  public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
  public BigDecimal getSubtotal() { return subtotal; }
  public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}

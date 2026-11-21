package com.jovanpets.document;

import com.jovanpets.domain.CategoriaProducto;
import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * Producto del catalogo con sus existencias. El stock solo cambia mediante operaciones atomicas
 * condicionadas (stock >= cantidad) y la edicion usa bloqueo optimista con {@link Version}.
 */
@Document("productos")
public class Producto {
  @Id private String id;
  @Indexed private String nombre;
  private String descripcion;
  @Indexed private CategoriaProducto categoria;

  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal precio;

  private int stock;
  private int stockMinimo;
  /** Medicamentos que exigen formula veterinaria: se consultan pero no se venden en linea. */
  private boolean requiereFormula;
  private boolean activo = true;
  @Version private Long version;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public String getDescripcion() { return descripcion; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public CategoriaProducto getCategoria() { return categoria; }
  public void setCategoria(CategoriaProducto categoria) { this.categoria = categoria; }
  public BigDecimal getPrecio() { return precio; }
  public void setPrecio(BigDecimal precio) { this.precio = precio; }
  public int getStock() { return stock; }
  public void setStock(int stock) { this.stock = stock; }
  public int getStockMinimo() { return stockMinimo; }
  public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
  public boolean isRequiereFormula() { return requiereFormula; }
  public void setRequiereFormula(boolean requiereFormula) { this.requiereFormula = requiereFormula; }
  public boolean isActivo() { return activo; }
  public void setActivo(boolean activo) { this.activo = activo; }
  public Long getVersion() { return version; }
  public void setVersion(Long version) { this.version = version; }

  /** HU-15: alerta cuando el stock actual es menor al minimo configurado. */
  public boolean stockBajo() {
    return stock < stockMinimo;
  }
}

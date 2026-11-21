package com.jovanpets.document;

import com.jovanpets.domain.CategoriaServicio;
import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/** Servicio veterinario o de peluqueria ofrecido por la clinica (HU-12). */
@Document("servicios")
public class Servicio extends RegistroActivable {
  @Id private String id;
  private String nombre;
  private String descripcion;
  private CategoriaServicio categoria;

  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal precio;

  private int duracionMinutos;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public String getDescripcion() { return descripcion; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public CategoriaServicio getCategoria() { return categoria; }
  public void setCategoria(CategoriaServicio categoria) { this.categoria = categoria; }
  public BigDecimal getPrecio() { return precio; }
  public void setPrecio(BigDecimal precio) { this.precio = precio; }
  public int getDuracionMinutos() { return duracionMinutos; }
  public void setDuracionMinutos(int duracionMinutos) { this.duracionMinutos = duracionMinutos; }
}

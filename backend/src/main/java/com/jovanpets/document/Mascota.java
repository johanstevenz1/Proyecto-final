package com.jovanpets.document;

import com.jovanpets.domain.Especie;
import com.jovanpets.domain.Sexo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/** Mascota. Referencia a su propietario mediante propietarioId (coleccion clientes). */
@Document("mascotas")
public class Mascota {
  @Id private String id;

  @Indexed private String propietarioId;

  private String nombre;
  private Especie especie;
  private String raza;
  private Sexo sexo;
  private LocalDate fechaNacimiento;

  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal peso;

  private boolean activo = true;
  private Instant fechaRegistro;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getPropietarioId() { return propietarioId; }
  public void setPropietarioId(String propietarioId) { this.propietarioId = propietarioId; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public Especie getEspecie() { return especie; }
  public void setEspecie(Especie especie) { this.especie = especie; }
  public String getRaza() { return raza; }
  public void setRaza(String raza) { this.raza = raza; }
  public Sexo getSexo() { return sexo; }
  public void setSexo(Sexo sexo) { this.sexo = sexo; }
  public LocalDate getFechaNacimiento() { return fechaNacimiento; }
  public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
  public BigDecimal getPeso() { return peso; }
  public void setPeso(BigDecimal peso) { this.peso = peso; }
  public boolean isActivo() { return activo; }
  public void setActivo(boolean activo) { this.activo = activo; }
  public Instant getFechaRegistro() { return fechaRegistro; }
  public void setFechaRegistro(Instant fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}

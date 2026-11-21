package com.jovanpets.document;

import com.jovanpets.domain.TipoAtencion;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entrada del historial clinico: antecedentes, controles, vacunas y desparasitaciones. Es una
 * coleccion propia (no se incrusta en Mascota) porque crece sin limite y se filtra por fechas.
 */
@Document("atenciones")
@CompoundIndex(name = "mascota_fecha", def = "{'mascotaId': 1, 'fecha': -1}")
public class Atencion {
  @Id private String id;
  private String mascotaId;
  private TipoAtencion tipo;
  private LocalDate fecha;
  /** Diagnostico, nombre de la vacuna o producto antiparasitario. */
  private String descripcion;
  private String observaciones;
  private String profesional;
  private LocalDate proximaFecha;
  private String registradoPor;
  private Instant fechaRegistro;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getMascotaId() { return mascotaId; }
  public void setMascotaId(String mascotaId) { this.mascotaId = mascotaId; }
  public TipoAtencion getTipo() { return tipo; }
  public void setTipo(TipoAtencion tipo) { this.tipo = tipo; }
  public LocalDate getFecha() { return fecha; }
  public void setFecha(LocalDate fecha) { this.fecha = fecha; }
  public String getDescripcion() { return descripcion; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public String getObservaciones() { return observaciones; }
  public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
  public String getProfesional() { return profesional; }
  public void setProfesional(String profesional) { this.profesional = profesional; }
  public LocalDate getProximaFecha() { return proximaFecha; }
  public void setProximaFecha(LocalDate proximaFecha) { this.proximaFecha = proximaFecha; }
  public String getRegistradoPor() { return registradoPor; }
  public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }
  public Instant getFechaRegistro() { return fechaRegistro; }
  public void setFechaRegistro(Instant fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}

package com.jovanpets.document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Propietario de mascotas. El historial de cambios se incrusta porque solo se consulta junto con
 * el cliente.
 */
@Document("clientes")
public class Cliente {
  @Id private String id;

  /** Bloqueo optimista ante modificaciones concurrentes. */
  @Version private Long version;

  @Indexed private String nombre;

  @Indexed(unique = true)
  private String cedula;

  @Indexed private String telefono;
  private String email;
  private String direccion;
  private boolean activo = true;
  private Instant fechaRegistro;
  private List<CambioCliente> historial = new ArrayList<>();

  public Long getVersion() { return version; }
  public void setVersion(Long version) { this.version = version; }
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public String getCedula() { return cedula; }
  public void setCedula(String cedula) { this.cedula = cedula; }
  public String getTelefono() { return telefono; }
  public void setTelefono(String telefono) { this.telefono = telefono; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getDireccion() { return direccion; }
  public void setDireccion(String direccion) { this.direccion = direccion; }
  public boolean isActivo() { return activo; }
  public void setActivo(boolean activo) { this.activo = activo; }
  public Instant getFechaRegistro() { return fechaRegistro; }
  public void setFechaRegistro(Instant fechaRegistro) { this.fechaRegistro = fechaRegistro; }
  public List<CambioCliente> getHistorial() { return historial; }
  public void setHistorial(List<CambioCliente> historial) { this.historial = historial; }
}

package com.jovanpets.document;

import com.jovanpets.domain.Rol;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Cuenta de acceso. Referencia opcionalmente la ficha de {@link Cliente} mediante clienteId. */
@Document("usuarios")
public class Usuario {
  @Id private String id;

  @Indexed(unique = true)
  private String email;

  private String claveHash;
  private String nombre;

  @Indexed private Rol rol;

  private boolean activo = true;

  @Indexed(sparse = true)
  private String clienteId;

  /** Se incrementa al cerrar sesion para invalidar los tokens emitidos. */
  private int versionSesion;

  private Instant fechaRegistro;
  private String recuperacionHash;
  private Instant recuperacionExpira;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getClaveHash() { return claveHash; }
  public void setClaveHash(String claveHash) { this.claveHash = claveHash; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public Rol getRol() { return rol; }
  public void setRol(Rol rol) { this.rol = rol; }
  public boolean isActivo() { return activo; }
  public void setActivo(boolean activo) { this.activo = activo; }
  public String getClienteId() { return clienteId; }
  public void setClienteId(String clienteId) { this.clienteId = clienteId; }
  public int getVersionSesion() { return versionSesion; }
  public void setVersionSesion(int versionSesion) { this.versionSesion = versionSesion; }
  public Instant getFechaRegistro() { return fechaRegistro; }
  public void setFechaRegistro(Instant fechaRegistro) { this.fechaRegistro = fechaRegistro; }
  public String getRecuperacionHash() { return recuperacionHash; }
  public void setRecuperacionHash(String recuperacionHash) { this.recuperacionHash = recuperacionHash; }
  public Instant getRecuperacionExpira() { return recuperacionExpira; }
  public void setRecuperacionExpira(Instant recuperacionExpira) { this.recuperacionExpira = recuperacionExpira; }
}

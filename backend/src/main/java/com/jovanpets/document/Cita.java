package com.jovanpets.document;

import com.jovanpets.domain.CategoriaServicio;
import com.jovanpets.domain.EstadoCita;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Cita veterinaria o de peluqueria. Referencia mascota, cliente y servicio por id y guarda una
 * copia de sus nombres para listar sin consultas adicionales.
 *
 * <p>Concurrencia: el indice unico parcial (agenda, franjas) impide que dos citas activas ocupen
 * la misma franja aunque lleguen solicitudes simultaneas. Al cancelar o rechazar la cita se marca
 * bloqueaAgenda=false y la franja vuelve a quedar libre.
 */
@Document("citas")
@CompoundIndex(
    name = "agenda_franja_unica",
    def = "{'agenda': 1, 'franjas': 1}",
    unique = true,
    partialFilter = "{'bloqueaAgenda': true}")
public class Cita {
  @Id private String id;

  /** Bloqueo optimista ante modificaciones concurrentes. */
  @Version private Long version;
  @Indexed private String mascotaId;
  private String mascotaNombre;
  @Indexed private String clienteId;
  private String clienteNombre;
  private String servicioId;
  private String servicioNombre;
  private CategoriaServicio agenda;
  @Indexed private LocalDateTime inicio;
  private LocalDateTime fin;
  private EstadoCita estado = EstadoCita.PENDIENTE;
  private String motivo;
  private String veterinarioId;
  private String veterinarioNombre;
  private List<LocalDateTime> franjas = new ArrayList<>();
  private boolean bloqueaAgenda = true;
  private Instant fechaSolicitud;

  public Long getVersion() { return version; }
  public void setVersion(Long version) { this.version = version; }
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getMascotaId() { return mascotaId; }
  public void setMascotaId(String mascotaId) { this.mascotaId = mascotaId; }
  public String getMascotaNombre() { return mascotaNombre; }
  public void setMascotaNombre(String mascotaNombre) { this.mascotaNombre = mascotaNombre; }
  public String getClienteId() { return clienteId; }
  public void setClienteId(String clienteId) { this.clienteId = clienteId; }
  public String getClienteNombre() { return clienteNombre; }
  public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
  public String getServicioId() { return servicioId; }
  public void setServicioId(String servicioId) { this.servicioId = servicioId; }
  public String getServicioNombre() { return servicioNombre; }
  public void setServicioNombre(String servicioNombre) { this.servicioNombre = servicioNombre; }
  public CategoriaServicio getAgenda() { return agenda; }
  public void setAgenda(CategoriaServicio agenda) { this.agenda = agenda; }
  public LocalDateTime getInicio() { return inicio; }
  public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
  public LocalDateTime getFin() { return fin; }
  public void setFin(LocalDateTime fin) { this.fin = fin; }
  public EstadoCita getEstado() { return estado; }
  public void setEstado(EstadoCita estado) { this.estado = estado; }
  public String getMotivo() { return motivo; }
  public void setMotivo(String motivo) { this.motivo = motivo; }
  public String getVeterinarioId() { return veterinarioId; }
  public void setVeterinarioId(String veterinarioId) { this.veterinarioId = veterinarioId; }
  public String getVeterinarioNombre() { return veterinarioNombre; }
  public void setVeterinarioNombre(String veterinarioNombre) { this.veterinarioNombre = veterinarioNombre; }
  public List<LocalDateTime> getFranjas() { return franjas; }
  public void setFranjas(List<LocalDateTime> franjas) { this.franjas = franjas; }
  public boolean isBloqueaAgenda() { return bloqueaAgenda; }
  public void setBloqueaAgenda(boolean bloqueaAgenda) { this.bloqueaAgenda = bloqueaAgenda; }
  public Instant getFechaSolicitud() { return fechaSolicitud; }
  public void setFechaSolicitud(Instant fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}

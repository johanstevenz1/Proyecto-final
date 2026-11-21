package com.jovanpets.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Secuencia atomica para numeros legibles (por ejemplo, el numero de pedido). */
@Document("contadores")
public class Contador {
  @Id private String id;
  private long secuencia;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public long getSecuencia() { return secuencia; }
  public void setSecuencia(long secuencia) { this.secuencia = secuencia; }
}

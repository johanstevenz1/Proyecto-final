package com.jovanpets.service;

import java.util.Map;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/** Comprueba que la API y la conexion con MongoDB estan operativas. */
@Service
public class EstadoService {
  private final MongoTemplate mongo;

  public EstadoService(MongoTemplate mongo) {
    this.mongo = mongo;
  }

  public Map<String, String> estado() {
    String baseDatos;
    try {
      mongo.executeCommand(new Document("ping", 1));
      baseDatos = "disponible";
    } catch (RuntimeException e) {
      baseDatos = "no disponible";
    }
    return Map.of("api", "disponible", "mongodb", baseDatos, "baseDatos", mongo.getDb().getName());
  }
}

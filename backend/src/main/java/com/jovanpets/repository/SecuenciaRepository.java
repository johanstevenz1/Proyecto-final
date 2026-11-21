package com.jovanpets.repository;

import com.jovanpets.document.Contador;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Genera secuencias unicas con un $inc atomico (upsert) sobre la coleccion contadores. */
@Repository
public class SecuenciaRepository {
  private final MongoTemplate mongo;

  public SecuenciaRepository(MongoTemplate mongo) {
    this.mongo = mongo;
  }

  public long siguiente(String nombre) {
    Contador c =
        mongo.findAndModify(
            Query.query(Criteria.where("_id").is(nombre)),
            new Update().inc("secuencia", 1),
            FindAndModifyOptions.options().returnNew(true).upsert(true),
            Contador.class);
    return c.getSecuencia();
  }
}

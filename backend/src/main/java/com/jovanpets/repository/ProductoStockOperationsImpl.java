package com.jovanpets.repository;

import com.jovanpets.document.Producto;
import com.jovanpets.domain.CategoriaProducto;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/** Implementacion con MongoTemplate del fragmento {@link ProductoStockOperations}. */
class ProductoStockOperationsImpl implements ProductoStockOperations {
  private final MongoTemplate mongo;

  ProductoStockOperationsImpl(MongoTemplate mongo) {
    this.mongo = mongo;
  }

  @Override
  public Optional<Producto> descontarStock(String productoId, int cantidad) {
    Query condicion =
        Query.query(Criteria.where("_id").is(productoId).and("activo").is(true).and("stock").gte(cantidad));
    // Tambien se incrementa la version para que una edicion concurrente detecte el cambio.
    Update cambio = new Update().inc("stock", -cantidad).inc("version", 1);
    return Optional.ofNullable(
        mongo.findAndModify(condicion, cambio, FindAndModifyOptions.options().returnNew(true), Producto.class));
  }

  @Override
  public Optional<Producto> incrementarStock(String productoId, int cantidad) {
    Query condicion = Query.query(Criteria.where("_id").is(productoId));
    Update cambio = new Update().inc("stock", cantidad).inc("version", 1);
    return Optional.ofNullable(
        mongo.findAndModify(condicion, cambio, FindAndModifyOptions.options().returnNew(true), Producto.class));
  }

  @Override
  public List<Producto> buscar(String texto, CategoriaProducto categoria, boolean soloActivos) {
    Criteria criterio = new Criteria();
    if (soloActivos) {
      criterio.and("activo").is(true);
    }
    if (categoria != null) {
      criterio.and("categoria").is(categoria);
    }
    if (texto != null && !texto.isBlank()) {
      criterio.and("nombre").regex(Pattern.quote(texto.trim()), "i");
    }
    return mongo.find(Query.query(criterio).with(Sort.by("nombre")), Producto.class);
  }
}

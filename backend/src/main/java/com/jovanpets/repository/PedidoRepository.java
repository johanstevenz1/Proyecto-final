package com.jovanpets.repository;

import com.jovanpets.document.Pedido;
import com.jovanpets.domain.EstadoPedido;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PedidoRepository extends MongoRepository<Pedido, String> {
  List<Pedido> findByClienteIdOrderByFechaDesc(String clienteId);

  List<Pedido> findAllByOrderByFechaDesc();

  List<Pedido> findByEstadoOrderByFechaDesc(EstadoPedido estado);

  List<Pedido> findTop5ByOrderByFechaDesc();

  long countByEstado(EstadoPedido estado);

  List<Pedido> findByFechaBetween(java.time.LocalDateTime desde, java.time.LocalDateTime hasta);
}

package com.jovanpets.repository;

import com.jovanpets.document.Usuario;
import com.jovanpets.domain.Rol;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
  Optional<Usuario> findByEmail(String email);

  Optional<Usuario> findByRecuperacionHash(String recuperacionHash);

  boolean existsByEmail(String email);

  boolean existsByClienteId(String clienteId);

  Optional<Usuario> findByClienteId(String clienteId);

  List<Usuario> findAllByOrderByNombreAsc();

  List<Usuario> findByRolOrderByNombreAsc(Rol rol);

  List<Usuario> findByRolInAndActivoTrueOrderByNombreAsc(List<Rol> roles);

  long countByRolAndActivoTrue(Rol rol);
}

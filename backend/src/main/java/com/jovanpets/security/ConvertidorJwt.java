package com.jovanpets.security;

import com.jovanpets.document.Usuario;
import com.jovanpets.repository.UsuarioRepository;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Convierte un JWT valido en la autenticacion de Spring Security. Consulta el usuario en cada
 * peticion para aplicar el rol vigente, rechazar cuentas desactivadas y tokens de sesiones
 * cerradas (claim "ver" distinto de versionSesion).
 */
@Component
public class ConvertidorJwt implements Converter<Jwt, AbstractAuthenticationToken> {
  private final UsuarioRepository usuarios;

  public ConvertidorJwt(UsuarioRepository usuarios) {
    this.usuarios = usuarios;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Usuario u =
        usuarios
            .findById(jwt.getSubject())
            .filter(Usuario::isActivo)
            .orElseThrow(() -> new BadCredentialsException("Sesion no valida"));
    Object version = jwt.getClaim(TokenService.CLAIM_VERSION);
    if (!(version instanceof Number n) || n.intValue() != u.getVersionSesion()) {
      throw new BadCredentialsException("La sesion fue cerrada");
    }
    UsuarioAutenticado principal =
        new UsuarioAutenticado(u.getId(), u.getEmail(), u.getNombre(), u.getRol(), u.getClienteId());
    return new UsernamePasswordAuthenticationToken(
        principal, jwt, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));
  }
}

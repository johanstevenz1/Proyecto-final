package com.jovanpets.security;

import com.jovanpets.document.Usuario;
import com.jovanpets.dto.SesionResponse;
import com.jovanpets.mapper.UsuarioMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Emite tokens JWT firmados con HMAC-SHA256 y de corta duracion (RNF-04). */
@Service
public class TokenService {
  static final String CLAIM_VERSION = "ver";

  private final JwtEncoder encoder;
  private final long minutos;

  public TokenService(JwtEncoder encoder, @Value("${app.jwt.minutos}") long minutos) {
    this.encoder = encoder;
    this.minutos = minutos;
  }

  public SesionResponse emitir(Usuario u) {
    Instant ahora = Instant.now();
    Instant expira = ahora.plus(minutos, ChronoUnit.MINUTES);
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("jovan-pets")
            .subject(u.getId())
            .issuedAt(ahora)
            .expiresAt(expira)
            .claim("rol", u.getRol().name())
            .claim(CLAIM_VERSION, u.getVersionSesion())
            .build();
    JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
    String token = encoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
    return new SesionResponse(token, expira, UsuarioMapper.aResponse(u));
  }
}

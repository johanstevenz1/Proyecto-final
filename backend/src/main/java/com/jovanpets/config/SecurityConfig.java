package com.jovanpets.config;

import com.jovanpets.security.ConvertidorJwt;
import com.jovanpets.security.ManejadorErroresSeguridad;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Autenticacion con token Bearer JWT (sin estado ni cookies, por eso CSRF no aplica) y
 * autorizacion por rol. Las reglas finas por rol se declaran con @PreAuthorize en los
 * controladores y las de propiedad de los datos en los servicios.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  @Bean
  PasswordEncoder passwordEncoder() {
    // BCrypt genera una sal aleatoria por contrasena (RNF-03, HU-18).
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecretKey claveJwt(@Value("${app.jwt.secreto}") String secreto) {
    byte[] bytes;
    if (secreto == null || secreto.isBlank()) {
      bytes = new byte[32];
      new SecureRandom().nextBytes(bytes);
      log.warn("JWT_SECRET no esta definido: se genero una clave temporal; las sesiones se pierden al reiniciar.");
    } else {
      bytes = secreto.getBytes(StandardCharsets.UTF_8);
      if (bytes.length < 32) {
        throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
      }
    }
    return new SecretKeySpec(bytes, "HmacSHA256");
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKey clave) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKey clave) {
    return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
  }

  @Bean
  SecurityFilterChain filtros(HttpSecurity http, ConvertidorJwt convertidor, ManejadorErroresSeguridad errores)
      throws Exception {
    http.csrf(c -> c.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(b -> b.disable())
        .formLogin(f -> f.disable())
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/auth/registro",
                        "/api/auth/login",
                        "/api/auth/recuperar",
                        "/api/auth/restablecer")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/productos/gestion", "/api/servicios/gestion")
                    .hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.GET, "/api/estado", "/api/servicios", "/api/productos", "/api/productos/*")
                    .permitAll()
                    .requestMatchers("/error").permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            o ->
                o.jwt(j -> j.jwtAuthenticationConverter(convertidor))
                    .authenticationEntryPoint(errores)
                    .accessDeniedHandler(errores))
        .exceptionHandling(e -> e.authenticationEntryPoint(errores).accessDeniedHandler(errores));
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes}") String origenes) {
    CorsConfiguration c = new CorsConfiguration();
    // Acepta origenes exactos (https://app.vercel.app) o patrones (https://*.vercel.app).
    c.setAllowedOriginPatterns(Arrays.stream(origenes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    c.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
    fuente.registerCorsConfiguration("/api/**", c);
    return fuente;
  }
}

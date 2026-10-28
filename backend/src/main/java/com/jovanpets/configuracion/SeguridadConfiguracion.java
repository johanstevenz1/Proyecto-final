package com.jovanpets.configuracion;

import com.jovanpets.repositorio.UsuarioRepositorio;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.web.cors.*;

@Configuration
public class SeguridadConfiguracion {
  @Bean
  PasswordEncoder codificador() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService usuarios(UsuarioRepositorio repo) {
    return email ->
        repo.findByEmailIgnoreCase(email.trim())
            .filter(u -> u.activo)
            .map(u -> User.withUsername(u.email).password(u.claveHash).roles(u.rol.name()).build())
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));
  }

  @Bean
  SecurityFilterChain filtros(HttpSecurity http) throws Exception {
    var manejador = new CsrfTokenRequestAttributeHandler();
    http.csrf(
            c ->
                c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(manejador))
        .cors(c -> {})
        .requestCache(c -> c.disable())
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/",
                        "/index.html",
                        "/app.js",
                        "/estilos.css",
                        "/config.js",
                        "/favicon.ico",
                        "/api/csrf",
                        "/api/estado",
                        "/api/registro",
                        "/api/login",
                        "/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/productos", "/api/servicios")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            f ->
                f.loginProcessingUrl("/api/login")
                    .usernameParameter("email")
                    .passwordParameter("clave")
                    .successHandler(
                        (req, res, auth) -> {
                          res.setContentType("application/json");
                          res.getWriter().write("{\"mensaje\":\"Sesion iniciada\"}");
                        })
                    .failureHandler(
                        (req, res, e) -> {
                          res.setStatus(401);
                          res.setContentType("application/json");
                          res.getWriter()
                              .write("{\"mensaje\":\"Correo o contrasena incorrectos\"}");
                        }))
        .logout(
            l ->
                l.logoutUrl("/api/logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler(
                        (req, res, auth) -> {
                          res.setContentType("application/json");
                          res.getWriter().write("{\"mensaje\":\"Sesion cerrada\"}");
                        }))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) -> {
                          res.setStatus(401);
                          res.setContentType("application/json");
                          res.getWriter().write("{\"mensaje\":\"Inicia sesion para continuar\"}");
                        })
                    .accessDeniedHandler(
                        (req, res, ex) -> {
                          res.setStatus(403);
                          res.setContentType("application/json");
                          res.getWriter()
                              .write(
                                  "{\"mensaje\":\"Solicitud no autorizada; actualiza la pagina\"}");
                        }));
    return http.build();
  }

  @Bean
  CorsConfigurationSource cors(
      @org.springframework.beans.factory.annotation.Value("${app.origenes}") String origenes) {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(Arrays.asList(origenes.split(",")));
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
    c.setAllowCredentials(true);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return source;
  }
}

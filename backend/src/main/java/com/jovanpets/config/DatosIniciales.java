package com.jovanpets.config;

import com.jovanpets.document.Usuario;
import com.jovanpets.domain.CategoriaProducto;
import com.jovanpets.domain.CategoriaServicio;
import com.jovanpets.domain.Rol;
import com.jovanpets.dto.ProductoRequest;
import com.jovanpets.dto.ServicioRequest;
import com.jovanpets.repository.ProductoRepository;
import com.jovanpets.repository.ServicioRepository;
import com.jovanpets.repository.UsuarioRepository;
import com.jovanpets.security.UsuarioAutenticado;
import com.jovanpets.service.AuthService;
import com.jovanpets.service.ProductoService;
import com.jovanpets.service.ServicioCatalogoService;
import java.math.BigDecimal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga inicial opcional. El administrador solo se crea si ADMIN_EMAIL y ADMIN_PASSWORD estan
 * definidos (no hay credenciales en el repositorio). El catalogo base se crea con
 * APP_DATOS_INICIALES=true y unicamente si las colecciones estan vacias.
 */
@Component
public class DatosIniciales implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

  private final UsuarioRepository usuarios;
  private final ServicioRepository servicios;
  private final ProductoRepository productos;
  private final ServicioCatalogoService catalogoServicios;
  private final ProductoService catalogoProductos;
  private final PasswordEncoder encoder;
  private final String adminEmail;
  private final String adminClave;
  private final String adminNombre;
  private final boolean datosIniciales;

  public DatosIniciales(
      UsuarioRepository usuarios,
      ServicioRepository servicios,
      ProductoRepository productos,
      ServicioCatalogoService catalogoServicios,
      ProductoService catalogoProductos,
      PasswordEncoder encoder,
      @Value("${app.admin.email}") String adminEmail,
      @Value("${app.admin.clave}") String adminClave,
      @Value("${app.admin.nombre}") String adminNombre,
      @Value("${app.datos-iniciales}") boolean datosIniciales) {
    this.usuarios = usuarios;
    this.servicios = servicios;
    this.productos = productos;
    this.catalogoServicios = catalogoServicios;
    this.catalogoProductos = catalogoProductos;
    this.encoder = encoder;
    this.adminEmail = adminEmail;
    this.adminClave = adminClave;
    this.adminNombre = adminNombre;
    this.datosIniciales = datosIniciales;
  }

  @Override
  public void run(ApplicationArguments args) {
    crearAdministrador();
    if (datosIniciales) {
      crearCatalogo();
    }
  }

  private void crearAdministrador() {
    if (adminEmail.isBlank() || adminClave.isBlank() || usuarios.countByRolAndActivoTrue(Rol.ADMINISTRADOR) > 0) {
      return;
    }
    if (adminClave.length() < 8) {
      log.warn("ADMIN_PASSWORD debe tener al menos 8 caracteres; no se creo el administrador");
      return;
    }
    Usuario u = new Usuario();
    u.setEmail(AuthService.normalizarEmail(adminEmail));
    u.setNombre(adminNombre);
    u.setClaveHash(encoder.encode(adminClave));
    u.setRol(Rol.ADMINISTRADOR);
    u.setFechaRegistro(Instant.now());
    usuarios.save(u);
    log.info("Administrador inicial creado: {}", u.getEmail());
  }

  private void crearCatalogo() {
    if (servicios.count() == 0) {
      servicio("Consulta general", "Valoracion medica completa de la mascota", CategoriaServicio.VETERINARIA, "45000", 30);
      servicio("Vacunacion", "Aplicacion de vacunas segun el plan de la mascota", CategoriaServicio.VETERINARIA, "35000", 15);
      servicio("Desparasitacion", "Control antiparasitario interno y externo", CategoriaServicio.VETERINARIA, "30000", 15);
      servicio("Bano y secado", "Bano con productos especiales y secado", CategoriaServicio.PELUQUERIA, "40000", 60);
      servicio("Corte de pelo", "Corte segun la raza y preferencia del propietario", CategoriaServicio.PELUQUERIA, "50000", 60);
      log.info("Servicios iniciales creados");
    }
    if (productos.count() == 0) {
      UsuarioAutenticado sistema = new UsuarioAutenticado("sistema", "sistema", "Carga inicial", Rol.ADMINISTRADOR, null);
      producto("Alimento premium perro adulto 2 kg", "Croquetas balanceadas para perros adultos", CategoriaProducto.ALIMENTOS, "68000", 25, 5, false, sistema);
      producto("Alimento gato esterilizado 1,5 kg", "Croquetas para gatos esterilizados", CategoriaProducto.ALIMENTOS, "59000", 18, 5, false, sistema);
      producto("Collar ajustable", "Collar de nailon con hebilla de seguridad", CategoriaProducto.ACCESORIOS, "22000", 30, 5, false, sistema);
      producto("Shampoo hipoalergenico", "Shampoo suave para piel sensible", CategoriaProducto.HIGIENE, "27000", 12, 4, false, sistema);
      producto("Pelota de caucho", "Juguete resistente para perros", CategoriaProducto.JUGUETES, "12000", 40, 8, false, sistema);
      producto("Antiparasitario oral perro", "Tableta antiparasitaria de amplio espectro", CategoriaProducto.MEDICAMENTOS, "18000", 3, 5, false, sistema);
      producto("Antibiotico veterinario", "Requiere formula del medico veterinario", CategoriaProducto.MEDICAMENTOS, "45000", 10, 3, true, sistema);
      log.info("Productos iniciales creados");
    }
  }

  private void servicio(String nombre, String descripcion, CategoriaServicio categoria, String precio, int minutos) {
    catalogoServicios.crear(new ServicioRequest(nombre, descripcion, categoria, new BigDecimal(precio), minutos, true));
  }

  private void producto(
      String nombre, String descripcion, CategoriaProducto categoria, String precio, int stock, int minimo,
      boolean formula, UsuarioAutenticado actor) {
    catalogoProductos.crear(
        new ProductoRequest(nombre, descripcion, categoria, new BigDecimal(precio), stock, minimo, formula), actor);
  }
}

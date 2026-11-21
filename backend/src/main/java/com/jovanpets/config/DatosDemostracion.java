package com.jovanpets.config;

import com.jovanpets.document.Usuario;
import com.jovanpets.domain.Especie;
import com.jovanpets.domain.ModalidadEntrega;
import com.jovanpets.domain.Rol;
import com.jovanpets.domain.Sexo;
import com.jovanpets.domain.TipoAtencion;
import com.jovanpets.dto.AtencionRequest;
import com.jovanpets.dto.CitaRequest;
import com.jovanpets.dto.CitaResponse;
import com.jovanpets.dto.ClienteRequest;
import com.jovanpets.dto.LineaPedidoRequest;
import com.jovanpets.dto.MascotaRequest;
import com.jovanpets.dto.MascotaResponse;
import com.jovanpets.dto.PedidoRequest;
import com.jovanpets.dto.ProductoResponse;
import com.jovanpets.dto.RegistroRequest;
import com.jovanpets.dto.ServicioResponse;
import com.jovanpets.dto.UsuarioCrearRequest;
import com.jovanpets.repository.UsuarioRepository;
import com.jovanpets.security.UsuarioAutenticado;
import com.jovanpets.service.AtencionService;
import com.jovanpets.service.AuthService;
import com.jovanpets.service.CitaService;
import com.jovanpets.service.ClienteService;
import com.jovanpets.service.MascotaService;
import com.jovanpets.service.PedidoService;
import com.jovanpets.service.ProductoService;
import com.jovanpets.service.ServicioCatalogoService;
import com.jovanpets.service.UsuarioService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Cuentas y datos de demostracion para la presentacion del proyecto: un cliente con mascotas,
 * historial, citas y un pedido; un veterinario; y algunas fichas de clientes registradas por el
 * administrador. Todo se crea a traves de los servicios, de modo que se aplican las mismas reglas
 * de negocio que en el uso normal. Solo se ejecuta si DEMO_CLIENTE_EMAIL y DEMO_CLIENTE_PASSWORD
 * estan definidos y la cuenta aun no existe.
 */
@Component
@Order(2)
public class DatosDemostracion implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(DatosDemostracion.class);

  private final UsuarioRepository usuarios;
  private final AuthService auth;
  private final UsuarioService usuarioService;
  private final ClienteService clientes;
  private final MascotaService mascotas;
  private final AtencionService atenciones;
  private final CitaService citas;
  private final ServicioCatalogoService servicios;
  private final ProductoService productos;
  private final PedidoService pedidos;
  private final Clock reloj;
  private final String clienteEmail;
  private final String clienteClave;
  private final String vetEmail;
  private final String vetClave;

  public DatosDemostracion(
      UsuarioRepository usuarios,
      AuthService auth,
      UsuarioService usuarioService,
      ClienteService clientes,
      MascotaService mascotas,
      AtencionService atenciones,
      CitaService citas,
      ServicioCatalogoService servicios,
      ProductoService productos,
      PedidoService pedidos,
      Clock reloj,
      @Value("${app.demo.cliente.email:}") String clienteEmail,
      @Value("${app.demo.cliente.clave:}") String clienteClave,
      @Value("${app.demo.veterinario.email:}") String vetEmail,
      @Value("${app.demo.veterinario.clave:}") String vetClave) {
    this.usuarios = usuarios;
    this.auth = auth;
    this.usuarioService = usuarioService;
    this.clientes = clientes;
    this.mascotas = mascotas;
    this.atenciones = atenciones;
    this.citas = citas;
    this.servicios = servicios;
    this.productos = productos;
    this.pedidos = pedidos;
    this.reloj = reloj;
    this.clienteEmail = clienteEmail;
    this.clienteClave = clienteClave;
    this.vetEmail = vetEmail;
    this.vetClave = vetClave;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (clienteEmail.isBlank() || clienteClave.length() < 8
        || usuarios.existsByEmail(AuthService.normalizarEmail(clienteEmail))) {
      return;
    }
    try {
      cargar();
      log.info("Datos de demostracion creados (cliente {}).", clienteEmail);
    } catch (RuntimeException e) {
      log.warn("No se pudieron crear todos los datos de demostracion: {}", e.getMessage());
    }
  }

  private void cargar() {
    Usuario admin =
        usuarios.findByRolOrderByNombreAsc(Rol.ADMINISTRADOR).stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("Se necesita un administrador inicial"));
    UsuarioAutenticado actorAdmin = actor(admin);

    UsuarioAutenticado veterinario = actorAdmin;
    if (!vetEmail.isBlank() && vetClave.length() >= 8 && !usuarios.existsByEmail(AuthService.normalizarEmail(vetEmail))) {
      usuarioService.crearPersonal(new UsuarioCrearRequest("Dra. Valentina Rios", vetEmail, vetClave, Rol.VETERINARIO));
    }
    Optional<Usuario> vet = usuarios.findByEmail(AuthService.normalizarEmail(vetEmail));
    if (vet.isPresent()) {
      veterinario = actor(vet.get());
    }

    // Cliente de demostracion registrado desde el portal (HU-18)
    auth.registrar(new RegistroRequest(
        "Laura Gomez", "1098765432", "3001112233", clienteEmail, "Calle 45 # 12-30, Bucaramanga", clienteClave));
    UsuarioAutenticado laura = actor(usuarios.findByEmail(AuthService.normalizarEmail(clienteEmail)).orElseThrow());

    LocalDate hoy = LocalDate.now(reloj);
    MascotaResponse max = mascotas.crear(new MascotaRequest(null, "Max", Especie.PERRO, "Labrador", Sexo.MACHO,
        hoy.minusYears(3).minusMonths(2), new BigDecimal("28.50")), laura);
    MascotaResponse luna = mascotas.crear(new MascotaRequest(null, "Luna", Especie.GATO, "Siames", Sexo.HEMBRA,
        hoy.minusYears(1).minusMonths(5), new BigDecimal("4.20")), laura);

    // Historial clinico (HU-06 a HU-08)
    atenciones.registrar(max.id(), new AtencionRequest(TipoAtencion.CONSULTA, hoy.minusMonths(4),
        "Otitis leve en oido derecho", "Limpieza y gotas por 7 dias. Control en un mes.", hoy.minusMonths(3)), veterinario);
    atenciones.registrar(max.id(), new AtencionRequest(TipoAtencion.CONTROL, hoy.minusMonths(3),
        "Control de otitis: resuelta", "Sin hallazgos adicionales.", null), veterinario);
    atenciones.registrar(max.id(), new AtencionRequest(TipoAtencion.VACUNA, hoy.minusMonths(11),
        "Rabia", "Lote aplicado sin reacciones.", hoy.plusMonths(1)), veterinario);
    atenciones.registrar(max.id(), new AtencionRequest(TipoAtencion.DESPARASITACION, hoy.minusMonths(2),
        "Antiparasitario oral de amplio espectro", null, hoy.plusMonths(1)), veterinario);
    atenciones.registrar(luna.id(), new AtencionRequest(TipoAtencion.VACUNA, hoy.minusMonths(6),
        "Triple felina", null, hoy.plusMonths(6)), veterinario);

    // Citas (HU-09, HU-10)
    List<ServicioResponse> activos = servicios.activos();
    ServicioResponse consulta = buscarServicio(activos, "Consulta");
    ServicioResponse bano = buscarServicio(activos, "Bano");
    if (consulta != null) {
      CitaResponse c1 = reservar(max.id(), consulta.id(), hoy.plusDays(1), laura);
      if (c1 != null) citas.confirmar(c1.id(), veterinario);
      reservar(luna.id(), consulta.id(), hoy.plusDays(3), laura);
    }
    if (bano != null) {
      reservar(max.id(), bano.id(), hoy.plusDays(5), laura);
    }

    // Pedido del cliente (HU-17)
    List<ProductoResponse> catalogo = productos.catalogo(null, null).stream().filter(p -> !p.requiereFormula() && p.stock() > 2).toList();
    if (catalogo.size() >= 2) {
      pedidos.crear(new PedidoRequest(List.of(
          new LineaPedidoRequest(catalogo.get(0).id(), 1), new LineaPedidoRequest(catalogo.get(1).id(), 2)),
          ModalidadEntrega.DOMICILIO, null, null), laura);
    }

    // Fichas registradas por el administrador (HU-01) con mascotas y pedidos
    String[][] otros = {
      {"Carlos Perez", "80123456", "3104567890", "carlos.perez@correo.com", "Carrera 27 # 45-10", "Toby", "PERRO", "Beagle"},
      {"Maria Lopez", "52987654", "3157778899", "maria.lopez@correo.com", "Calle 56 # 30-21", "Mia", "GATO", "Persa"},
      {"Andres Ruiz", "1095123456", "3209876543", "andres.ruiz@correo.com", "Avenida 1 # 9-80", "Kiwi", "AVE", "Periquito"},
    };
    int k = 0;
    for (String[] o : otros) {
      var cliente = clientes.crear(new ClienteRequest(o[0], o[1], o[2], o[3], o[4]), actorAdmin);
      MascotaResponse m = mascotas.crear(new MascotaRequest(cliente.id(), o[5], Especie.valueOf(o[6]), o[7],
          k % 2 == 0 ? Sexo.MACHO : Sexo.HEMBRA, hoy.minusYears(2 + k), new BigDecimal(k == 2 ? "0.10" : "9.80")), actorAdmin);
      if (consulta != null) {
        reservar(m.id(), consulta.id(), hoy.plusDays(2 + k), actorAdmin);
      }
      if (catalogo.size() > k + 2) {
        pedidos.crear(new PedidoRequest(List.of(new LineaPedidoRequest(catalogo.get(k + 2).id(), 1 + k)),
            k == 1 ? ModalidadEntrega.DOMICILIO : ModalidadEntrega.RECOGIDA, null, cliente.id()), actorAdmin);
      }
      k++;
    }
  }

  /** Reserva el primer horario libre del dia indicado (o de los siguientes). */
  private CitaResponse reservar(String mascotaId, String servicioId, LocalDate desde, UsuarioAutenticado actor) {
    for (int i = 0; i < 5; i++) {
      List<LocalDateTime> libres = citas.disponibilidad(servicioId, desde.plusDays(i)).horarios();
      if (!libres.isEmpty()) {
        LocalDateTime hora = libres.get(Math.min(libres.size() - 1, 4 + i * 3));
        return citas.solicitar(new CitaRequest(mascotaId, servicioId, hora), actor);
      }
    }
    return null;
  }

  private static ServicioResponse buscarServicio(List<ServicioResponse> lista, String prefijo) {
    return lista.stream().filter(s -> s.nombre().startsWith(prefijo)).findFirst().orElse(null);
  }

  private static UsuarioAutenticado actor(Usuario u) {
    return new UsuarioAutenticado(u.getId(), u.getEmail(), u.getNombre(), u.getRol(), u.getClienteId());
  }
}

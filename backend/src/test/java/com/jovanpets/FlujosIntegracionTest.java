package com.jovanpets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jovanpets.document.Producto;
import com.jovanpets.document.Usuario;
import com.jovanpets.domain.Rol;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Pruebas de integracion de los procesos principales contra un MongoDB embebido y aislado
 * (Flapdoodle). Recorren controller -> service -> repository -> MongoDB.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlujosIntegracionTest {
  private static final String CLAVE = "Clave-segura-123";

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired MongoTemplate mongo;
  @Autowired PasswordEncoder encoder;
  @Autowired Clock reloj;

  private String admin;
  private String veterinario;

  @BeforeEach
  void limpiarBaseDePruebas() throws Exception {
    // Salvaguarda: las pruebas solo limpian la base aislada de pruebas, nunca datos reales.
    assertThat(mongo.getDb().getName()).isEqualTo("jovanpets_pruebas");
    for (String coleccion : mongo.getCollectionNames()) {
      mongo.getCollection(coleccion).deleteMany(new Document());
    }
    admin = crearPersonal("admin@jovanpets.test", "Admin Pruebas", Rol.ADMINISTRADOR);
    veterinario = crearPersonal("vet@jovanpets.test", "Dra. Pruebas", Rol.VETERINARIO);
  }

  // ---------------------------------------------------------------- autenticacion

  @Test
  void registroInicioDeSesionYPerfil() throws Exception {
    String token = registrarCliente("ana@correo.com", "1001001001");
    enviar(get("/api/auth/yo"), token, null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rol").value("CLIENTE"))
        .andExpect(jsonPath("$.clienteId").isNotEmpty());

    enviar(post("/api/auth/login"), null, Map.of("email", "ANA@correo.com", "clave", CLAVE)).andExpect(status().isOk());
    enviar(post("/api/auth/login"), null, Map.of("email", "ana@correo.com", "clave", "incorrecta1"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.mensaje").value("Correo o contrasena incorrectos"));

    // HU-18: email unico; contrasena con hash
    enviar(post("/api/auth/registro"), null, registro("ana@correo.com", "2002002002")).andExpect(status().isConflict());
    Usuario guardado = mongo.findAll(Usuario.class).stream().filter(u -> u.getEmail().equals("ana@correo.com")).findFirst().orElseThrow();
    assertThat(guardado.getClaveHash()).isNotEqualTo(CLAVE).startsWith("$2");
  }

  @Test
  void validacionesDeEntradaDevuelvenErroresPorCampo() throws Exception {
    Map<String, Object> invalido = registro("no-es-email", "12");
    invalido.put("clave", "corta");
    enviar(post("/api/auth/registro"), null, invalido)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errores.email").exists())
        .andExpect(jsonPath("$.errores.cedula").exists())
        .andExpect(jsonPath("$.errores.clave").exists());
  }

  @Test
  void cerrarSesionInvalidaElTokenYElCambioDeRolEsInmediato() throws Exception {
    String token = registrarCliente("luis@correo.com", "3003003003");
    enviar(get("/api/inventario/alertas"), token, null).andExpect(status().isForbidden())
        .andExpect(jsonPath("$.mensaje").value("Acceso denegado"));

    String usuarioId = leer(enviar(get("/api/auth/yo"), token, null)).get("id").asText();
    enviar(patch("/api/usuarios/" + usuarioId + "/rol"), admin, Map.of("rol", "VETERINARIO")).andExpect(status().isOk());
    enviar(get("/api/inventario/alertas"), token, null).andExpect(status().isOk()); // HU-20: acceso inmediato

    enviar(post("/api/auth/logout"), token, null).andExpect(status().isOk());
    enviar(get("/api/auth/yo"), token, null).andExpect(status().isUnauthorized());
  }

  @Test
  void endpointsProtegidosPorRol() throws Exception {
    String cliente = registrarCliente("eva@correo.com", "4004004004");
    enviar(get("/api/clientes"), null, null).andExpect(status().isUnauthorized());
    enviar(get("/api/clientes"), cliente, null).andExpect(status().isForbidden());
    enviar(get("/api/usuarios"), veterinario, null).andExpect(status().isForbidden());
    enviar(post("/api/productos"), veterinario, producto("X", 1, 1, false)).andExpect(status().isForbidden());
    enviar(get("/api/clientes"), admin, null).andExpect(status().isOk());
    enviar(get("/api/productos"), null, null).andExpect(status().isOk()); // catalogo publico
    enviar(get("/api/estado"), null, null).andExpect(jsonPath("$.mongodb").value("disponible"));
  }

  @Test
  void recuperacionDeContrasenaConTokenTemporal() throws Exception {
    registrarCliente("olvido@correo.com", "5005005005");
    enviar(post("/api/auth/recuperar"), null, Map.of("email", "olvido@correo.com")).andExpect(status().isOk());
    enviar(post("/api/auth/recuperar"), null, Map.of("email", "noexiste@correo.com")).andExpect(status().isOk());
    Usuario u = mongo.findAll(Usuario.class).stream().filter(x -> x.getEmail().equals("olvido@correo.com")).findFirst().orElseThrow();
    assertThat(u.getRecuperacionHash()).isNotBlank();
    assertThat(u.getRecuperacionExpira()).isAfter(java.time.Instant.now().plusSeconds(23 * 3600));
    enviar(post("/api/auth/restablecer"), null, Map.of("token", "token-falso", "nuevaClave", "OtraClave-456"))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------- clientes y mascotas

  @Test
  void gestionDeClientesConCedulaUnicaHistorialYBusqueda() throws Exception {
    Map<String, Object> datos = cliente("Carlos Perez", "7007007007", "3101234567");
    String id = leer(enviar(post("/api/clientes"), admin, datos).andExpect(status().isCreated())).get("id").asText();
    enviar(post("/api/clientes"), admin, datos).andExpect(status().isConflict());

    datos.put("telefono", "3209998877");
    enviar(put("/api/clientes/" + id), admin, datos).andExpect(status().isOk());
    datos.put("cedula", "999");
    enviar(put("/api/clientes/" + id), admin, datos).andExpect(status().isBadRequest());

    enviar(get("/api/clientes/" + id), admin, null)
        .andExpect(jsonPath("$.historial[0].detalle").value(org.hamcrest.Matchers.containsString("telefono")));

    enviar(get("/api/clientes?q=carl"), admin, null).andExpect(jsonPath("$.totalElementos").value(1));
    enviar(get("/api/clientes?q=7007007007"), admin, null).andExpect(jsonPath("$.totalElementos").value(1));
    enviar(get("/api/clientes?q=320999"), admin, null).andExpect(jsonPath("$.totalElementos").value(1));
    enviar(get("/api/clientes?q=(.*"), admin, null).andExpect(status().isOk()); // texto escapado

    enviar(delete("/api/clientes/" + id), admin, null).andExpect(status().isNoContent());
    enviar(get("/api/clientes?q=carl"), admin, null).andExpect(jsonPath("$.totalElementos").value(0));
  }

  @Test
  void cadaClienteSoloAccedeASusMascotasEHistorial() throws Exception {
    String ana = registrarCliente("ana@correo.com", "1001001001");
    String beto = registrarCliente("beto@correo.com", "1001001002");
    String mascotaId = crearMascota(ana, null);

    enviar(get("/api/mascotas/" + mascotaId), beto, null).andExpect(status().isForbidden());
    enviar(get("/api/mascotas"), beto, null).andExpect(jsonPath("$.length()").value(0));
    enviar(get("/api/mascotas"), ana, null).andExpect(jsonPath("$.length()").value(1));

    Map<String, Object> vacuna = Map.of(
        "tipo", "VACUNA", "fecha", LocalDate.now(reloj).toString(), "descripcion", "Rabia",
        "proximaFecha", LocalDate.now(reloj).plusYears(1).toString());
    enviar(post("/api/mascotas/" + mascotaId + "/atenciones"), ana, vacuna).andExpect(status().isForbidden());
    enviar(post("/api/mascotas/" + mascotaId + "/atenciones"), veterinario, vacuna).andExpect(status().isCreated());
    enviar(get("/api/mascotas/" + mascotaId + "/atenciones"), ana, null)
        .andExpect(jsonPath("$[0].profesional").value("Dra. Pruebas"));
    enviar(get("/api/mascotas/" + mascotaId + "/atenciones"), beto, null).andExpect(status().isForbidden());
  }

  // ---------------------------------------------------------------- citas

  @Test
  void noSePermitenCitasEnHorariosOcupadosYCancelarLiberaLaFranja() throws Exception {
    String ana = registrarCliente("ana@correo.com", "1001001001");
    String beto = registrarCliente("beto@correo.com", "1001001002");
    String mascotaAna = crearMascota(ana, null);
    String mascotaBeto = crearMascota(beto, null);
    String servicio = crearServicio("Consulta", "VETERINARIA", 30);
    LocalDate dia = LocalDate.now(reloj).plusDays(3);
    LocalDateTime nueve = dia.atTime(9, 0);

    String citaId = leer(enviar(post("/api/citas"), ana, cita(mascotaAna, servicio, nueve)).andExpect(status().isCreated()))
        .get("id").asText();
    // Solapamiento parcial (9:15) en la misma agenda
    enviar(post("/api/citas"), beto, cita(mascotaBeto, servicio, dia.atTime(9, 15))).andExpect(status().isConflict());
    enviar(post("/api/citas"), beto, cita(mascotaAna, servicio, dia.atTime(10, 0))).andExpect(status().isForbidden());
    enviar(post("/api/citas"), beto, cita(mascotaBeto, servicio, dia.atTime(12, 0))).andExpect(status().isBadRequest());

    JsonNode libres = leer(enviar(get("/api/citas/disponibilidad?servicioId=" + servicio + "&fecha=" + dia), beto, null));
    assertThat(texto(libres.get("horarios"))).doesNotContain(nueve.toString() + ":00").contains(dia.atTime(9, 30) + ":00");

    enviar(patch("/api/citas/" + citaId + "/confirmar"), ana, null).andExpect(status().isForbidden());
    enviar(patch("/api/citas/" + citaId + "/confirmar"), veterinario, null)
        .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
        .andExpect(jsonPath("$.veterinarioNombre").value("Dra. Pruebas"));

    enviar(patch("/api/citas/" + citaId + "/cancelar"), beto, Map.of("motivo", "x")).andExpect(status().isForbidden());
    enviar(patch("/api/citas/" + citaId + "/cancelar"), ana, Map.of("motivo", "Viaje")).andExpect(jsonPath("$.estado").value("CANCELADA"));
    // HU-23: el horario cancelado queda disponible para otros clientes
    enviar(post("/api/citas"), beto, cita(mascotaBeto, servicio, nueve)).andExpect(status().isCreated());

    enviar(get("/api/citas?desde=" + dia + "&hasta=" + dia), veterinario, null).andExpect(jsonPath("$.length()").value(2));
    enviar(get("/api/citas"), ana, null).andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void lasAgendasDeVeterinariaYPeluqueriaSonIndependientes() throws Exception {
    String ana = registrarCliente("ana@correo.com", "1001001001");
    String mascota = crearMascota(ana, null);
    String consulta = crearServicio("Consulta", "VETERINARIA", 30);
    String bano = crearServicio("Bano", "PELUQUERIA", 60);
    LocalDateTime hora = LocalDate.now(reloj).plusDays(2).atTime(15, 0);
    enviar(post("/api/citas"), ana, cita(mascota, consulta, hora)).andExpect(status().isCreated());
    enviar(post("/api/citas"), ana, cita(mascota, bano, hora)).andExpect(status().isCreated());
  }

  // ---------------------------------------------------------------- pedidos e inventario

  @Test
  void pedidoDescuentaInventarioYCancelarloLoRepone() throws Exception {
    String ana = registrarCliente("ana@correo.com", "1001001001");
    String collar = leer(enviar(post("/api/productos"), admin, producto("Collar", 5, 2, false)).andExpect(status().isCreated()))
        .get("id").asText();
    String receta = leer(enviar(post("/api/productos"), admin, producto("Antibiotico", 5, 1, true))).get("id").asText();

    var lineas = java.util.List.of(Map.of("productoId", collar, "cantidad", 2), Map.of("productoId", collar, "cantidad", 1));
    enviar(post("/api/pedidos/cotizar"), ana, Map.of("lineas", lineas)).andExpect(jsonPath("$.total").value(75000.0));

    JsonNode pedido =
        leer(enviar(post("/api/pedidos"), ana, Map.of("lineas", lineas, "modalidad", "RECOGIDA")).andExpect(status().isCreated()));
    assertThat(pedido.get("estado").asText()).isEqualTo("PENDIENTE");
    assertThat(pedido.get("numero").asText()).matches("JP-\\d{4}-\\d{6}");
    assertThat(stock(collar)).isEqualTo(2);

    // Sin existencias suficientes: no se crea el pedido y el stock no cambia
    enviar(post("/api/pedidos"), ana, Map.of("lineas", java.util.List.of(Map.of("productoId", collar, "cantidad", 3)), "modalidad", "RECOGIDA"))
        .andExpect(status().isConflict());
    assertThat(stock(collar)).isEqualTo(2);
    enviar(post("/api/pedidos"), ana, Map.of("lineas", java.util.List.of(Map.of("productoId", receta, "cantidad", 1)), "modalidad", "RECOGIDA"))
        .andExpect(status().isBadRequest());
    enviar(post("/api/pedidos"), ana, Map.of("lineas", java.util.List.of(), "modalidad", "RECOGIDA")).andExpect(status().isBadRequest());

    enviar(get("/api/inventario/alertas"), veterinario, null).andExpect(jsonPath("$.length()").value(0));
    enviar(get("/api/inventario/movimientos?productoId=" + collar), admin, null)
        .andExpect(jsonPath("$[0].tipo").value("SALIDA"))
        .andExpect(jsonPath("$[0].saldo").value(2));

    String pedidoId = pedido.get("id").asText();
    enviar(patch("/api/pedidos/" + pedidoId + "/cancelar"), ana, Map.of("motivo", "Ya no lo necesito"))
        .andExpect(jsonPath("$.estado").value("CANCELADO"));
    assertThat(stock(collar)).isEqualTo(5);
    enviar(patch("/api/pedidos/" + pedidoId + "/cancelar"), ana, Map.of("motivo", "otra vez")).andExpect(status().isBadRequest());
  }

  @Test
  void movimientosDeInventarioNoDejanStockNegativo() throws Exception {
    String id = leer(enviar(post("/api/productos"), admin, producto("Shampoo", 4, 3, false))).get("id").asText();
    enviar(post("/api/inventario/movimientos"), veterinario, movimiento(id, "SALIDA", 5)).andExpect(status().isConflict());
    enviar(post("/api/inventario/movimientos"), veterinario, movimiento(id, "SALIDA", 2))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.saldo").value(2));
    enviar(get("/api/inventario/alertas"), veterinario, null).andExpect(jsonPath("$[0].id").value(id));
    enviar(post("/api/inventario/movimientos"), veterinario, movimiento(id, "ENTRADA", 10)).andExpect(jsonPath("$.saldo").value(12));
    enviar(post("/api/inventario/movimientos"), veterinario, movimiento(id, "ENTRADA", 0)).andExpect(status().isBadRequest());
    assertThat(stock(id)).isEqualTo(12);
  }

  @Test
  void elPanelResumeLasMetricasDelNegocio() throws Exception {
    registrarCliente("ana@correo.com", "1001001001");
    enviar(post("/api/productos"), admin, producto("Pocas unidades", 1, 5, false));
    enviar(get("/api/panel"), admin, null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalClientes").value(1))
        .andExpect(jsonPath("$.alertasStock.length()").value(1));
  }

  @Test
  void mongoDbTieneLosIndicesDeUnicidadYConcurrencia() {
    assertThat(indices("clientes")).contains("cedula");
    assertThat(indices("usuarios")).contains("email");
    assertThat(indices("citas")).contains("agenda_franja_unica");
    assertThat(indices("pedidos")).contains("numero");
  }

  // ---------------------------------------------------------------- utilidades

  private String crearPersonal(String email, String nombre, Rol rol) throws Exception {
    Usuario u = new Usuario();
    u.setEmail(email);
    u.setNombre(nombre);
    u.setRol(rol);
    u.setClaveHash(encoder.encode(CLAVE));
    mongo.save(u);
    return leer(enviar(post("/api/auth/login"), null, Map.of("email", email, "clave", CLAVE))).get("token").asText();
  }

  private String registrarCliente(String email, String cedula) throws Exception {
    return leer(enviar(post("/api/auth/registro"), null, registro(email, cedula)).andExpect(status().isCreated()))
        .get("token").asText();
  }

  private String crearMascota(String token, String propietarioId) throws Exception {
    Map<String, Object> m = new java.util.HashMap<>(Map.of(
        "nombre", "Firulais", "especie", "PERRO", "raza", "Criollo", "sexo", "MACHO",
        "fechaNacimiento", "2021-05-01", "peso", 12.5));
    if (propietarioId != null) {
      m.put("propietarioId", propietarioId);
    }
    return leer(enviar(post("/api/mascotas"), token, m).andExpect(status().isCreated())).get("id").asText();
  }

  private String crearServicio(String nombre, String categoria, int minutos) throws Exception {
    return leer(enviar(post("/api/servicios"), admin, Map.of(
        "nombre", nombre, "descripcion", "Servicio de prueba", "categoria", categoria, "precio", 40000,
        "duracionMinutos", minutos)).andExpect(status().isCreated())).get("id").asText();
  }

  private static Map<String, Object> registro(String email, String cedula) {
    Map<String, Object> r = cliente("Cliente " + cedula, cedula, "3001234567");
    r.put("email", email);
    r.put("clave", CLAVE);
    return r;
  }

  private static Map<String, Object> cliente(String nombre, String cedula, String telefono) {
    return new java.util.HashMap<>(Map.of(
        "nombre", nombre, "cedula", cedula, "telefono", telefono, "email", cedula + "@correo.com",
        "direccion", "Calle 10 # 20-30"));
  }

  private static Map<String, Object> producto(String nombre, int stock, int minimo, boolean formula) {
    return Map.of(
        "nombre", nombre, "descripcion", "Producto de prueba", "categoria", formula ? "MEDICAMENTOS" : "ACCESORIOS",
        "precio", 25000, "stockInicial", stock, "stockMinimo", minimo, "requiereFormula", formula);
  }

  private static Map<String, Object> cita(String mascota, String servicio, LocalDateTime inicio) {
    return Map.of("mascotaId", mascota, "servicioId", servicio, "inicio", inicio.toString());
  }

  private static Map<String, Object> movimiento(String producto, String tipo, int cantidad) {
    return Map.of("productoId", producto, "tipo", tipo, "cantidad", cantidad, "motivo", "Prueba");
  }

  private int stock(String productoId) {
    return mongo.findById(productoId, Producto.class).getStock();
  }

  private String indices(String coleccion) {
    return StreamSupport.stream(mongo.getCollection(coleccion).listIndexes().spliterator(), false)
        .map(Document::toJson)
        .reduce("", String::concat);
  }

  private static String texto(JsonNode nodo) {
    return nodo.toString();
  }

  private ResultActions enviar(MockHttpServletRequestBuilder peticion, String token, Object cuerpo) throws Exception {
    if (token != null) {
      peticion.header("Authorization", "Bearer " + token);
    }
    if (cuerpo != null) {
      peticion.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(cuerpo));
    }
    return mvc.perform(peticion);
  }

  private JsonNode leer(ResultActions r) throws Exception {
    return json.readTree(r.andReturn().getResponse().getContentAsString());
  }
}

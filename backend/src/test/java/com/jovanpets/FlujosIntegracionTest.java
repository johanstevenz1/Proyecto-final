package com.jovanpets;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.jovanpets.dominio.*;
import com.jovanpets.repositorio.*;
import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:pruebas;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "app.demo=true"
    })
@AutoConfigureMockMvc
class FlujosIntegracionTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired ProductoRepositorio productos;
  @Autowired PedidoRepositorio pedidos;
  @Autowired ServicioRepositorio servicios;
  @Autowired MascotaRepositorio mascotas;
  @Autowired UsuarioRepositorio usuarios;
  @Autowired CitaRepositorio citas;

  @Test
  void noPermiteCancelarCitasPasadasNiCambiarSuEstado() throws Exception {
    MockHttpSession sesion = login("cliente@jovanpets.test");
    Cita cita = new Cita();
    cita.mascota = mascotas.findAll().getFirst();
    cita.servicio = servicios.findAll().getFirst();
    cita.inicio = LocalDateTime.now(ZoneId.of("America/Bogota")).minusDays(1);
    cita.fin = cita.inicio.plusMinutes(30);
    cita = citas.save(cita);
    mvc.perform(patch("/api/citas/" + cita.id).session(sesion).with(csrf())
        .contentType("application/json")
        .content("{\"estado\":\"CANCELADA\",\"motivo\":\"Cambio de planes\"}"))
        .andExpect(status().isBadRequest());
    assertThat(citas.findById(cita.id).orElseThrow().estado).isEqualTo(EstadoCita.PENDIENTE);
  }

  @Test
  void rechazaProductoConPrecioCeroYNoLoPersiste() throws Exception {
    MockHttpSession sesion = login("admin@jovanpets.test");
    long antes = productos.count();
    mvc.perform(post("/api/productos").session(sesion).with(csrf())
        .contentType("application/json")
        .content("{\"nombre\":\"Precio cero\",\"categoria\":\"ACCESORIOS\",\"precio\":0,\"stock\":2}"))
        .andExpect(status().isBadRequest());
    assertThat(productos.count()).isEqualTo(antes);
  }

  MockHttpSession login(String email) throws Exception {
    return (MockHttpSession)
        mvc.perform(
                post("/api/login")
                    .with(csrf())
                    .param("email", email)
                    .param("clave", "JovanDemo2026!"))
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession();
  }

  JsonNode respuesta(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder b)
      throws Exception {
    return json.readTree(
        mvc.perform(b).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
  }

  @Test
  void protegeSesionCsrfYRoles() throws Exception {
    mvc.perform(get("/api/mascotas")).andExpect(status().isUnauthorized());
    MockHttpSession cliente = login("cliente@jovanpets.test");
    mvc.perform(post("/api/pedidos").session(cliente).contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/usuarios").session(cliente)).andExpect(status().isForbidden());
    mvc.perform(get("/api/sesion").session(cliente))
        .andExpect(jsonPath("$.claveHash").doesNotExist());
    MockHttpSession personal = login("personal@jovanpets.test");
    mvc.perform(
            post("/api/citas")
                .session(personal)
                .with(csrf())
                .contentType("application/json")
                .content("{\"mascotaId\":1,\"servicioId\":1,\"inicio\":\"2030-01-10T09:00:00\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void pedidoCalculaTotalDescuentaYDevuelveStockUnaSolaVez() throws Exception {
    MockHttpSession s = login("cliente@jovanpets.test");
    Producto p = new Producto();
    p.nombre = "Producto de prueba pedido";
    p.categoria = "ACCESORIOS";
    p.precio = new BigDecimal("12500.00");
    p.stock = 5;
    p.stockMinimo = 1;
    p = productos.save(p);
    String compra =
        "{\"modalidad\":\"RECOGIDA\",\"total\":1,\"lineas\":[{\"productoId\":"
            + p.id
            + ",\"cantidad\":2}]}";
    JsonNode pedido =
        respuesta(
            post("/api/pedidos")
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content(compra));
    assertThat(pedido.get("total").decimalValue()).isEqualByComparingTo("25000.00");
    assertThat(productos.findById(p.id).orElseThrow().stock).isEqualTo(3);
    Long id = pedido.get("id").asLong();
    respuesta(
        patch("/api/pedidos/" + id)
            .session(s)
            .with(csrf())
            .contentType("application/json")
            .content("{\"estado\":\"CANCELADO\"}"));
    assertThat(productos.findById(p.id).orElseThrow().stock).isEqualTo(5);
    mvc.perform(
            patch("/api/pedidos/" + id)
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content("{\"estado\":\"CANCELADO\"}"))
        .andExpect(status().isBadRequest());
    assertThat(productos.findById(p.id).orElseThrow().stock).isEqualTo(5);
  }

  @Test
  void pedidoSinExistenciasRevierteTodaLaTransaccion() throws Exception {
    MockHttpSession s = login("cliente@jovanpets.test");
    Producto a = new Producto();
    a.nombre = "Disponible rollback";
    a.categoria = "ACCESORIOS";
    a.precio = BigDecimal.TEN;
    a.stock = 3;
    a = productos.save(a);
    Producto b = new Producto();
    b.nombre = "Agotado rollback";
    b.categoria = "ACCESORIOS";
    b.precio = BigDecimal.TEN;
    b.stock = 0;
    b = productos.save(b);
    long antes = pedidos.count();
    mvc.perform(
            post("/api/pedidos")
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"modalidad\":\"RECOGIDA\",\"lineas\":[{\"productoId\":"
                        + a.id
                        + ",\"cantidad\":1},{\"productoId\":"
                        + b.id
                        + ",\"cantidad\":1}]}"))
        .andExpect(status().isConflict());
    assertThat(productos.findById(a.id).orElseThrow().stock).isEqualTo(3);
    assertThat(pedidos.count()).isEqualTo(antes);
  }

  @Test
  void clienteNoAccedeAMascotaAjena() throws Exception {
    MockHttpSession s = login("cliente@jovanpets.test");
    mvc.perform(get("/api/mascotas/99999/historial").session(s)).andExpect(status().isNotFound());
    String registro =
        "{\"nombre\":\"Otro cliente"
            + " prueba\",\"cedula\":\"OTRO-TEST\",\"email\":\"otro@test.example\",\"clave\":\"OtraClave123!\",\"telefono\":\"3001234567\",\"direccion\":\"Direccion"
            + " ficticia\"}";
    mvc.perform(
            post("/api/registro").with(csrf()).contentType("application/json").content(registro))
        .andExpect(status().isCreated());
    MockHttpSession otro =
        (MockHttpSession)
            mvc.perform(
                    post("/api/login")
                        .with(csrf())
                        .param("email", "otro@test.example")
                        .param("clave", "OtraClave123!"))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();
    Long mascota = mascotas.findAll().getFirst().id;
    mvc.perform(get("/api/mascotas/" + mascota + "/historial").session(otro))
        .andExpect(status().isForbidden());
  }

  @Test
  void reservaImpideSolapamientoPorDuracionYLiberacionAlCancelar() throws Exception {
    MockHttpSession s = login("cliente@jovanpets.test");
    Long m = mascotas.findAll().getFirst().id;
    Long servicio =
        servicios.findAll().stream()
            .filter(x -> x.categoria.equals("VETERINARIA"))
            .findFirst()
            .orElseThrow()
            .id;
    String fecha = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(20).toString();
    String body =
        "{\"mascotaId\":"
            + m
            + ",\"servicioId\":"
            + servicio
            + ",\"inicio\":\""
            + fecha
            + "T09:00:00\"}";
    JsonNode c =
        respuesta(
            post("/api/citas")
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content(body));
    mvc.perform(
            post("/api/citas")
                .session(s)
                .with(csrf())
                .contentType("application/json")
                .content(body.replace("09:00:00", "09:15:00")))
        .andExpect(status().isConflict());
    respuesta(
        patch("/api/citas/" + c.get("id").asLong())
            .session(s)
            .with(csrf())
            .contentType("application/json")
            .content("{\"estado\":\"CANCELADA\",\"motivo\":\"Cambio de planes\"}"));
    respuesta(
        post("/api/citas").session(s).with(csrf()).contentType("application/json").content(body));
  }

  @Test
  void registroNoPermiteAutoasignarRol() throws Exception {
    String body =
        "{\"nombre\":\"Cliente prueba"
            + " rol\",\"cedula\":\"ROL-TEST\",\"email\":\"rol@test.example\",\"clave\":\"ClaveSegura123!\",\"telefono\":\"3001234567\",\"direccion\":\"Direccion"
            + " ficticia\",\"rol\":\"ADMINISTRADOR\"}";
    mvc.perform(post("/api/registro").with(csrf()).contentType("application/json").content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rol").value("CLIENTE"));
  }

  @Test
  void dosComprasConcurrentesNoVendenLaMismaUnidad() throws Exception {
    MockHttpSession sesion = login("cliente@jovanpets.test");
    Producto p = new Producto();
    p.nombre = "Ultima unidad concurrente";
    p.categoria = "ACCESORIOS";
    p.precio = BigDecimal.TEN;
    p.stock = 1;
    p = productos.save(p);
    String cuerpo =
        "{\"modalidad\":\"RECOGIDA\",\"lineas\":[{\"productoId\":" + p.id + ",\"cantidad\":1}]}";
    java.util.concurrent.Callable<Integer> comprar =
        () ->
            mvc.perform(
                    post("/api/pedidos")
                        .session(sesion)
                        .with(csrf())
                        .contentType("application/json")
                        .content(cuerpo))
                .andReturn()
                .getResponse()
                .getStatus();
    assertThat(simultaneas(comprar)).containsExactly(200, 409);
    assertThat(productos.findById(p.id).orElseThrow().stock).isZero();
  }

  @Test
  void dosReservasConcurrentesNoOcupanElMismoIntervalo() throws Exception {
    MockHttpSession sesion = login("cliente@jovanpets.test");
    Long mascota = mascotas.findAll().getFirst().id;
    Long servicio =
        servicios.findAll().stream()
            .filter(x -> x.categoria.equals("PELUQUERIA"))
            .findFirst()
            .orElseThrow()
            .id;
    String fecha = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(35).toString();
    String cuerpo =
        "{\"mascotaId\":"
            + mascota
            + ",\"servicioId\":"
            + servicio
            + ",\"inicio\":\""
            + fecha
            + "T10:00:00\"}";
    java.util.concurrent.Callable<Integer> reservar =
        () ->
            mvc.perform(
                    post("/api/citas")
                        .session(sesion)
                        .with(csrf())
                        .contentType("application/json")
                        .content(cuerpo))
                .andReturn()
                .getResponse()
                .getStatus();
    assertThat(simultaneas(reservar)).containsExactly(200, 409);
  }

  private java.util.List<Integer> simultaneas(java.util.concurrent.Callable<Integer> accion)
      throws Exception {
    var salida = new java.util.concurrent.CountDownLatch(1);
    var listas = new java.util.concurrent.CountDownLatch(2);
    try (var ejecutor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
      java.util.concurrent.Callable<Integer> tarea =
          () -> {
            listas.countDown();
            salida.await();
            return accion.call();
          };
      var a = ejecutor.submit(tarea);
      var b = ejecutor.submit(tarea);
      assertThat(listas.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
      salida.countDown();
      return java.util.stream.Stream.of(
              a.get(15, java.util.concurrent.TimeUnit.SECONDS),
              b.get(15, java.util.concurrent.TimeUnit.SECONDS))
          .sorted()
          .toList();
    }
  }
}

package com.jovanpets.controlador;

import com.jovanpets.controlador.Solicitudes.*;
import com.jovanpets.dominio.*;
import com.jovanpets.servicio.*;
import jakarta.validation.Valid;
import java.time.*;
import java.util.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiControlador {
  private final IUsuarios usuarios;
  private final IClientesMascotas clientes;
  private final ICitas citas;
  private final IVentas ventas;
  private final IPanel panel;
  private final Acceso acceso;

  public ApiControlador(
      IUsuarios u, IClientesMascotas c, ICitas ci, IVentas v, IPanel p, Acceso a) {
    usuarios = u;
    clientes = c;
    citas = ci;
    ventas = v;
    panel = p;
    acceso = a;
  }

  @GetMapping("/estado")
  public Map<String, String> estado() {
    return Map.of("estado", "disponible", "aplicacion", "Jovan Pets");
  }

  @GetMapping("/csrf")
  public Map<String, String> csrf(CsrfToken token) {
    return Map.of("token", token.getToken(), "cabecera", token.getHeaderName());
  }

  @PostMapping("/registro")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Usuario registro(@Valid @RequestBody Registro e) {
    return usuarios.registrar(e);
  }

  @GetMapping("/sesion")
  public Usuario sesion() {
    return acceso.actual();
  }

  @GetMapping("/clientes")
  public List<Cliente> clientes(@RequestParam(defaultValue = "") String q) {
    return clientes.clientes(q);
  }

  @PostMapping("/clientes")
  public Cliente cliente(@RequestBody Cliente e) {
    return clientes.guardarCliente(null, e);
  }

  @PutMapping("/clientes/{id}")
  public Cliente cliente(@PathVariable Long id, @RequestBody Cliente e) {
    return clientes.guardarCliente(id, e);
  }

  @GetMapping("/mascotas")
  public List<Mascota> mascotas() {
    return clientes.mascotas();
  }

  @PostMapping("/mascotas")
  public Mascota mascota(@RequestBody Mascota e) {
    return clientes.guardarMascota(null, e);
  }

  @PutMapping("/mascotas/{id}")
  public Mascota mascota(@PathVariable Long id, @RequestBody Mascota e) {
    return clientes.guardarMascota(id, e);
  }

  @GetMapping("/mascotas/{id}/historial")
  public List<Atencion> historial(@PathVariable Long id) {
    return clientes.historial(id);
  }

  @PostMapping("/mascotas/{id}/historial")
  public Atencion historial(@PathVariable Long id, @RequestBody Atencion e) {
    return clientes.atender(id, e);
  }

  @GetMapping("/servicios")
  public List<Servicio> servicios() {
    return citas.servicios();
  }

  @PostMapping("/servicios")
  public Servicio servicio(@RequestBody Servicio e) {
    return citas.guardarServicio(null, e);
  }

  @PutMapping("/servicios/{id}")
  public Servicio servicio(@PathVariable Long id, @RequestBody Servicio e) {
    return citas.guardarServicio(id, e);
  }

  @GetMapping("/citas")
  public List<Cita> citas(@RequestParam(required = false) LocalDate fecha) {
    return citas.listar(fecha);
  }

  @GetMapping("/citas/disponibilidad")
  public List<LocalDateTime> libres(@RequestParam Long servicioId, @RequestParam LocalDate fecha) {
    return citas.disponibilidad(servicioId, fecha);
  }

  @PostMapping("/citas")
  public Cita cita(@Valid @RequestBody Reserva e) {
    return citas.reservar(e);
  }

  @PatchMapping("/citas/{id}")
  public Cita cita(@PathVariable Long id, @Valid @RequestBody CambioCita e) {
    return citas.cambiar(id, e);
  }

  @PutMapping("/citas/{id}/horario")
  public Cita reagendar(@PathVariable Long id, @Valid @RequestBody Reserva e) {
    return citas.reagendar(id, e);
  }

  @GetMapping("/productos")
  public List<Producto> productos(@RequestParam(defaultValue = "") String q) {
    return ventas.productos(q);
  }

  @PostMapping("/productos")
  public Producto producto(@RequestBody Producto e) {
    return ventas.guardarProducto(null, e);
  }

  @PutMapping("/productos/{id}")
  public Producto producto(@PathVariable Long id, @RequestBody Producto e) {
    return ventas.guardarProducto(id, e);
  }

  @GetMapping("/inventario/movimientos")
  public List<MovimientoInventario> movimientos() {
    return ventas.movimientos();
  }

  @PostMapping("/inventario/movimientos")
  public MovimientoInventario movimiento(@Valid @RequestBody Movimiento e) {
    return ventas.mover(e);
  }

  @GetMapping("/inventario/alertas")
  public List<Producto> alertas() {
    return ventas.alertas();
  }

  @GetMapping("/pedidos")
  public List<Pedido> pedidos() {
    return ventas.pedidos();
  }

  @PostMapping("/pedidos")
  public Pedido pedido(@Valid @RequestBody Compra e) {
    return ventas.comprar(e);
  }

  @PatchMapping("/pedidos/{id}")
  public Pedido pedido(@PathVariable Long id, @Valid @RequestBody CambioPedido e) {
    return ventas.cambiarPedido(id, e.estado());
  }

  @GetMapping("/usuarios")
  public List<Usuario> usuarios() {
    return usuarios.listar();
  }

  @PatchMapping("/usuarios/{id}")
  public Usuario usuario(@PathVariable Long id, @Valid @RequestBody CambioRol e) {
    return usuarios.cambiar(id, e);
  }

  @GetMapping("/panel")
  public Map<String, Object> panel() {
    return panel.resumen();
  }
}

package com.jovanpets.configuracion;

import com.jovanpets.dominio.*;
import com.jovanpets.repositorio.*;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatosIniciales {
  @Bean
  CommandLineRunner cargar(
      UsuarioRepositorio usuarios,
      ClienteRepositorio clientes,
      MascotaRepositorio mascotas,
      ServicioRepositorio servicios,
      ProductoRepositorio productos,
      MovimientoInventarioRepositorio movimientos,
      AgendaRepositorio agendas,
      PasswordEncoder claves,
      @org.springframework.beans.factory.annotation.Value("${app.demo}") boolean demo) {
    return args -> {
      for (String nombre : new String[] {"VETERINARIA", "PELUQUERIA"})
        if (agendas.findAll().stream().noneMatch(a -> a.nombre.equals(nombre))) {
          Agenda a = new Agenda();
          a.nombre = nombre;
          agendas.save(a);
        }
      if (!demo || usuarios.count() > 0) return;
      Cliente c = new Cliente();
      c.nombre = "Ana Torres (demostracion)";
      c.cedula = "DEMO-1001";
      c.email = "cliente@jovanpets.test";
      c.telefono = "3000000000";
      c.direccion = "Direccion de demostracion";
      clientes.save(c);
      String[] emails = {"admin@jovanpets.test", "personal@jovanpets.test", c.email};
      Rol[] roles = {Rol.ADMINISTRADOR, Rol.PERSONAL, Rol.CLIENTE};
      for (int i = 0; i < 3; i++) {
        Usuario u = new Usuario();
        u.email = emails[i];
        u.rol = roles[i];
        u.claveHash = claves.encode("JovanDemo2026!");
        u.cliente = i == 2 ? c : null;
        usuarios.save(u);
      }
      Mascota m = new Mascota();
      m.nombre = "Luna";
      m.especie = "Perro";
      m.raza = "Mestiza";
      m.sexo = "Hembra";
      m.fechaNacimiento = java.time.LocalDate.of(2022, 3, 12);
      m.peso = new BigDecimal("12.50");
      m.propietario = c;
      mascotas.save(m);
      String[] nombres = {"Consulta general", "Vacunacion", "Peluqueria completa"};
      int[] duraciones = {30, 30, 60};
      String[] precios = {"45000", "35000", "55000"};
      for (int i = 0; i < 3; i++) {
        Servicio s = new Servicio();
        s.nombre = nombres[i];
        s.descripcion = "Servicio para el cuidado de tu mascota";
        s.categoria = i == 2 ? "PELUQUERIA" : "VETERINARIA";
        s.precio = new BigDecimal(precios[i]);
        s.duracionMinutos = duraciones[i];
        servicios.save(s);
      }
      String[] pn = {
        "Alimento premium 2 kg", "Shampoo antipulgas", "Juguete mordedor", "Snack dental"
      };
      String[] pc = {"ALIMENTO", "HIGIENE", "ACCESORIOS", "ALIMENTO"};
      int[] stock = {18, 3, 12, 24};
      int[] min = {5, 5, 4, 6};
      String[] pp = {"48000", "25000", "18000", "12000"};
      for (int i = 0; i < pn.length; i++) {
        Producto pr = new Producto();
        pr.nombre = pn[i];
        pr.descripcion = "Cuidado y bienestar para tu mascota";
        pr.categoria = pc[i];
        pr.precio = new BigDecimal(pp[i]);
        pr.stock = stock[i];
        pr.stockMinimo = min[i];
        productos.save(pr);
        MovimientoInventario mov = new MovimientoInventario();
        mov.producto = pr;
        mov.fecha = java.time.LocalDateTime.now(java.time.ZoneId.of("America/Bogota"));
        mov.tipo = TipoMovimiento.ENTRADA;
        mov.cantidad = stock[i];
        mov.motivo = "Existencias iniciales de demostracion";
        mov.saldo = stock[i];
        movimientos.save(mov);
      }
    };
  }
}

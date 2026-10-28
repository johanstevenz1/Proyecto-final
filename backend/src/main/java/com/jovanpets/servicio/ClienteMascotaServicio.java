package com.jovanpets.servicio;

import static com.jovanpets.servicio.Reglas.*;

import com.jovanpets.dominio.*;
import com.jovanpets.repositorio.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteMascotaServicio implements IClientesMascotas {
  private final ClienteRepositorio clientes;
  private final MascotaRepositorio mascotas;
  private final AtencionRepositorio atenciones;
  private final Acceso acceso;

  public ClienteMascotaServicio(
      ClienteRepositorio c, MascotaRepositorio m, AtencionRepositorio a, Acceso s) {
    clientes = c;
    mascotas = m;
    atenciones = a;
    acceso = s;
  }

  public List<Cliente> clientes(String q) {
    var u = acceso.actual();
    return u.rol == Rol.CLIENTE ? List.of(u.cliente) : clientes.buscar(q == null ? "" : q);
  }

  @Transactional
  public Cliente guardarCliente(Long id, Cliente e) {
    if (id == null) acceso.personal();
    else acceso.propietario(id);
    Cliente c = id == null ? new Cliente() : existe(clientes.findById(id));
    c.nombre = texto(e.nombre, "Nombre", 120);
    String cedula = texto(e.cedula, "Cedula", 30);
    conflicto(
        clientes.findByCedula(cedula).filter(x -> !Objects.equals(x.id, id)).isPresent(),
        "La cedula ya esta registrada");
    exigir(
        id == null || java.util.Objects.equals(c.cedula, cedula),
        "La cedula no se modifica desde la ficha");
    c.cedula = cedula;
    c.telefono = texto(e.telefono, "Telefono", 40);
    c.direccion = texto(e.direccion, "Direccion", 250);
    c.email = texto(e.email, "Correo", 180);
    exigir(c.email.matches("^[^ @]+@[^ @]+[.][^ @]+$"), "Correo no valido");
    if (acceso.esPersonal()) c.activo = e.activo;
    return clientes.save(c);
  }

  public List<Mascota> mascotas() {
    var u = acceso.actual();
    return u.rol == Rol.CLIENTE
        ? mascotas.findByPropietarioIdOrderByNombre(u.cliente.id)
        : mascotas.findAll();
  }

  public Mascota mascota(Long id) {
    var m = existe(mascotas.findById(id));
    acceso.propietario(m.propietario.id);
    return m;
  }

  @Transactional
  public Mascota guardarMascota(Long id, Mascota e) {
    Mascota m = id == null ? new Mascota() : mascota(id);
    var u = acceso.actual();
    Long propietario =
        u.rol == Rol.CLIENTE ? u.cliente.id : e.propietario == null ? null : e.propietario.id;
    exigir(propietario != null, "Selecciona un propietario");
    Cliente c = existe(clientes.findById(propietario));
    exigir(c.activo, "El cliente esta inactivo");
    m.propietario = c;
    m.nombre = texto(e.nombre, "Nombre", 100);
    m.especie = texto(e.especie, "Especie", 60);
    m.raza = texto(e.raza, "Raza", 100);
    m.sexo = texto(e.sexo, "Sexo", 20);
    exigir(
        e.fechaNacimiento == null
            || !e.fechaNacimiento.isAfter(LocalDate.now(java.time.ZoneId.of("America/Bogota"))),
        "La fecha de nacimiento no puede ser futura");
    exigir(
        e.peso != null && e.peso.signum() > 0 && e.peso.scale() <= 2 && e.peso.precision() <= 10,
        "El peso debe ser mayor que cero");
    m.fechaNacimiento = e.fechaNacimiento;
    m.peso = e.peso;
    m.activo = e.activo;
    return mascotas.save(m);
  }

  public List<Atencion> historial(Long id) {
    mascota(id);
    return atenciones.findByMascotaIdOrderByFechaDescIdDesc(id);
  }

  @Transactional
  public Atencion atender(Long id, Atencion e) {
    acceso.personal();
    Mascota m = mascota(id);
    exigir(e.tipo != null && e.fecha != null, "Tipo y fecha son obligatorios");
    exigir(
        !e.fecha.isAfter(LocalDate.now(java.time.ZoneId.of("America/Bogota"))),
        "La atencion no puede ser futura");
    exigir(
        e.proximaFecha == null || !e.proximaFecha.isBefore(e.fecha),
        "La proxima fecha no puede ser anterior a la atencion");
    Atencion a = new Atencion();
    a.mascota = m;
    a.tipo = e.tipo;
    a.fecha = e.fecha;
    a.descripcion = texto(e.descripcion, "Descripcion", 4000);
    a.profesional = acceso.actual().email;
    a.proximaFecha = e.proximaFecha;
    return atenciones.save(a);
  }
}

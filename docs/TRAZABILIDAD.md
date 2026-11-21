# Matriz de trazabilidad

HU = historia de usuario ([HISTORIAS-DE-USUARIO.md](HISTORIAS-DE-USUARIO.md)). Prueba: `IT` = `FlujosIntegracionTest`, `UT` = pruebas unitarias, `E2E` = verificación manual en el navegador con MongoDB real.

| HU | Pantalla (frontend) | Controller / endpoint | Service | Documento / colección | Prueba | Estado |
|---|---|---|---|---|---|---|
| HU-01 Registrar cliente | `Clientes` (modal) | `ClienteController` POST `/api/clientes` | `ClienteService.crear` | `Cliente` / clientes (cédula única) | IT gestionDeClientes… | Completo |
| HU-02 Editar cliente | `Clientes`, `ClienteDetallePagina` | PUT `/api/clientes/{id}` | `ClienteService.actualizar` | `Cliente.historial[]` | IT gestionDeClientes… | Completo |
| HU-03 Buscar clientes | `Clientes` | GET `/api/clientes?q=` | `ClienteService.buscar` | índices nombre, cédula, teléfono | IT gestionDeClientes… | Completo |
| HU-04 Listado de clientes | `Clientes` (paginación y orden) | GET `/api/clientes` | `ClienteService.buscar` | clientes | IT | Completo (eliminar = desactivar) |
| HU-05 Registrar mascota | `MisMascotas`, `MascotasAdmin`, `ClienteDetallePagina` | `MascotaController` POST | `MascotaService.crear` | `Mascota` / mascotas | IT cadaClienteSoloAccede…, E2E | Completo |
| HU-06 Antecedentes | `FichaMascota` | POST `/api/mascotas/{id}/atenciones`, PUT/DELETE `/api/atenciones/{id}` | `AtencionService` | `Atencion` / atenciones | IT | Completo |
| HU-07 Historial de controles | `FichaMascota` (filtro de fechas y detalle expandible) | GET `/api/mascotas/{id}/atenciones` | `AtencionService.historial` | atenciones | IT | Completo |
| HU-08 Vacunas y desparasitaciones | `FichaMascota` (secciones separadas) | POST atenciones (tipo VACUNA/DESPARASITACION) | `AtencionService` | `Atencion.proximaFecha` | IT | Completo |
| HU-09 Solicitar cita | `SolicitarCita`, `SelectorHorario` | GET `/api/citas/disponibilidad`, POST `/api/citas` | `CitaService.disponibilidad/solicitar` | `Cita` / citas | IT noSePermitenCitas…, E2E | Completo |
| HU-10 Confirmar o rechazar | `AgendaCitas` (pendientes) | PATCH confirmar / rechazar | `CitaService` | citas | IT, E2E | Completo (aviso por correo pendiente; el cliente ve el estado en *Mis citas*) |
| HU-11 Calendario | `AgendaCitas` (día y semana, filtro por veterinario) | GET `/api/citas?desde&hasta&veterinarioId` | `CitaService.listar` | citas | IT, E2E | Completo |
| HU-12 Servicios | `ServiciosAdmin`, `Inicio` | `ServicioController` | `ServicioCatalogoService` | servicios | IT | Completo |
| HU-13 Registrar producto | `ProductosAdmin` | `ProductoController` POST | `ProductoService.crear` | productos + movimiento inicial | IT | Completo |
| HU-14 Entradas y salidas | `Inventario` | POST `/api/inventario/movimientos` | `InventarioService.registrar` | movimientos_inventario | IT movimientosDeInventario… | Completo |
| HU-15 Alertas de stock | `Inventario`, `Dashboard` | GET `/api/inventario/alertas` | `InventarioService.alertas` | consulta `$expr stock < stockMinimo` | IT | Completo |
| HU-16 Catálogo y carrito | `Catalogo`, `Carrito` | GET `/api/productos`, POST `/api/pedidos/cotizar` | `ProductoService`, `PedidoService.cotizar` | productos | UT PedidoServiceTest, E2E | Completo |
| HU-17 Realizar pedido | `Carrito` (confirmación) | POST `/api/pedidos` | `PedidoService.crear` | pedidos, productos, movimientos | IT pedidoDescuentaInventario…, UT, E2E | Completo |
| HU-18 Registro de usuario | `Registro` | POST `/api/auth/registro` | `AuthService.registrar` | usuarios + clientes | IT registro…, E2E | Completo (correo de confirmación opcional: no implementado) |
| HU-19 Iniciar sesión | `Login` | POST `/api/auth/login` | `AuthService.iniciarSesion` | usuarios | IT, E2E | Completo |
| HU-20 Asignar roles | `Usuarios` | PATCH `/api/usuarios/{id}/rol` | `UsuarioService.cambiarRol` | usuarios | IT cerrarSesion…CambioDeRol… | Completo |
| HU-21 Control de acceso | `RutaProtegida`, `AccesoDenegado` | `@PreAuthorize` + validaciones de propiedad | todos | — | IT endpointsProtegidosPorRol, E2E | Completo |
| HU-22 Recuperar contraseña | `RecuperarClave`, `RestablecerClave` | POST recuperar / restablecer | `AuthService` + `ServicioCorreo` | `Usuario.recuperacionHash` | IT recuperacion… | Parcial: falta el proveedor de correo |
| HU-23 Cancelar cita | `MisCitas` | PATCH `/api/citas/{id}/cancelar` | `CitaService.cancelar` | índice parcial de franjas | IT | Completo |
| HU-24 Dashboard | `Dashboard` | GET `/api/panel` | `PanelService` | varias | IT elPanelResume…, E2E | Completo |

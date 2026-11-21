# API REST

Base: `http://localhost:8080`. Autenticación con `Authorization: Bearer <token>`. Errores con el formato `{fecha, estado, mensaje, errores}`.

Roles: **C** = Cliente, **V** = Personal veterinario, **A** = Administrador, **P** = público.

| Método | Ruta | Roles | Descripción | HU |
|---|---|---|---|---|
| POST | `/api/auth/registro` | P | Registro con rol Cliente y su ficha | HU-18 |
| POST | `/api/auth/login` | P | Inicio de sesión, devuelve el token | HU-19 |
| POST | `/api/auth/renovar` | C V A | Renueva el token mientras hay actividad | RNF-04 |
| POST | `/api/auth/logout` | C V A | Cierra la sesión e invalida los tokens | RU-03 |
| GET | `/api/auth/yo` | C V A | Usuario actual | — |
| PUT | `/api/auth/clave` | C V A | Cambio de contraseña | RU-05 |
| POST | `/api/auth/recuperar` | P | Enlace de recuperación de 24 horas | HU-22 |
| POST | `/api/auth/restablecer` | P | Nueva contraseña con el enlace | HU-22 |
| GET | `/api/usuarios?rol=` | A | Usuarios por rol | HU-20 |
| GET | `/api/usuarios/personal` | V A | Personal activo (filtro de calendario) | HU-11 |
| POST | `/api/usuarios` | A | Crear cuenta de personal | HU-20 |
| PATCH | `/api/usuarios/{id}/rol` | A | Cambiar rol | HU-20 |
| PATCH | `/api/usuarios/{id}/estado` | A | Activar o desactivar | ROL-05 |
| GET | `/api/clientes?q=&pagina=&tamano=&orden=` | V A | Búsqueda paginada | HU-03, HU-04 |
| POST | `/api/clientes` | A | Registrar cliente | HU-01 |
| GET | `/api/clientes/{id}` | V A (C la suya) | Ficha con historial y mascotas | HU-02 |
| PUT | `/api/clientes/{id}` | A | Editar (la cédula no cambia) | HU-02 |
| DELETE | `/api/clientes/{id}` | A | Desactivar | RC-05 |
| GET / PUT | `/api/clientes/me` | C | Perfil propio | — |
| GET | `/api/mascotas?propietarioId=` | C (propias) V A | Listado | HU-05 |
| POST | `/api/mascotas` | C V A | Registrar | HU-05 |
| GET / PUT / DELETE | `/api/mascotas/{id}` | C (propia) V A | Consultar, editar, desactivar | RM-05 |
| GET | `/api/mascotas/{id}/atenciones?desde=&hasta=` | C (propia) V A | Historial clínico | HU-07 |
| POST | `/api/mascotas/{id}/atenciones` | V A | Antecedente, vacuna o desparasitación | HU-06, HU-08 |
| PUT / DELETE | `/api/atenciones/{id}` | V A | Editar o eliminar una entrada | HU-06 |
| GET | `/api/servicios` | P | Servicios activos | HU-12 |
| GET | `/api/servicios/gestion` | A | Todos los servicios | HU-12 |
| POST / PUT | `/api/servicios`, `/api/servicios/{id}` | A | Registrar o editar | HU-12 |
| PATCH | `/api/servicios/{id}/estado` | A | Activar o desactivar | HU-12 |
| GET | `/api/citas/disponibilidad?servicioId=&fecha=` | C V A | Horarios libres | HU-09 |
| GET | `/api/citas?desde=&hasta=&estado=&veterinarioId=` | C (propias) V A | Citas y calendario | HU-10, HU-11 |
| POST | `/api/citas` | C A | Solicitar cita | HU-09 |
| PATCH | `/api/citas/{id}/confirmar` | V A | Confirmar | HU-10 |
| PATCH | `/api/citas/{id}/rechazar` | V A | Rechazar con motivo | HU-10 |
| PATCH | `/api/citas/{id}/atender` | V A | Marcar atendida | — |
| PATCH | `/api/citas/{id}/cancelar` | C (propia) V A | Cancelar cita futura con motivo | HU-23 |
| PATCH | `/api/citas/{id}/reprogramar` | C (propia) V A | Reprogramar | RA-07 |
| GET | `/api/productos?q=&categoria=` | P | Catálogo activo | HU-16 |
| GET | `/api/productos/gestion` | A | Todos los productos | RP-05 |
| GET | `/api/productos/{id}` | P | Detalle | — |
| POST / PUT | `/api/productos`, `/api/productos/{id}` | A | Registrar o editar | HU-13 |
| PATCH | `/api/productos/{id}/estado` | A | Activar o desactivar | RP-05 |
| POST | `/api/inventario/movimientos` | V A | Entrada o salida | HU-14 |
| GET | `/api/inventario/movimientos?productoId=` | V A | Historial | RI-05 |
| GET | `/api/inventario/alertas` | V A | Stock bajo | HU-15 |
| POST | `/api/pedidos/cotizar` | C A | Total del carrito calculado en el backend | HU-16 |
| POST | `/api/pedidos` | C A | Confirmar pedido | HU-17 |
| GET | `/api/pedidos?estado=` | C (propios) A | Pedidos | RPE-04 |
| GET | `/api/pedidos/{id}` | C (propio) A | Detalle | RPE-04 |
| PATCH | `/api/pedidos/{id}/cancelar` | C (propio) A | Cancelar antes del despacho | RPE-05 |
| PATCH | `/api/pedidos/{id}/estado` | A | Avanzar estado | — |
| GET | `/api/panel` | V A | Métricas del dashboard | HU-24 |
| GET | `/api/estado` | P | Estado de la API y de MongoDB | — |

Códigos usados: 200, 201, 204, 400 (validación o regla de negocio), 401 (sin sesión válida), 403 (acceso denegado), 404, 409 (duplicado, horario ocupado, stock insuficiente o modificación concurrente).

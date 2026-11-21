// Servicios de comunicacion con la API REST, agrupados por modulo.
import type {
  Atencion,
  AtencionDatos,
  CategoriaProducto,
  Cita,
  Cliente,
  ClienteDatos,
  ClienteDetalle,
  Cotizacion,
  Disponibilidad,
  EstadoCita,
  EstadoPedido,
  LineaCarrito,
  Mascota,
  MascotaDatos,
  ModalidadEntrega,
  Movimiento,
  Pagina,
  Panel,
  Pedido,
  Producto,
  ProductoDatos,
  RegistroDatos,
  Rol,
  Servicio,
  ServicioDatos,
  Sesion,
  TipoMovimiento,
  Usuario,
} from '../interfaces/tipos';
import { api, consulta } from './api';

interface Mensaje {
  mensaje: string;
}

export const authService = {
  registrar: (d: RegistroDatos) => api.post<Sesion>('/api/auth/registro', d),
  login: (email: string, clave: string) => api.post<Sesion>('/api/auth/login', { email, clave }),
  logout: () => api.post<Mensaje>('/api/auth/logout'),
  yo: () => api.get<Usuario>('/api/auth/yo'),
  cambiarClave: (claveActual: string, nuevaClave: string) =>
    api.put<Sesion>('/api/auth/clave', { claveActual, nuevaClave }),
  recuperar: (email: string) => api.post<Mensaje>('/api/auth/recuperar', { email }),
  restablecer: (token: string, nuevaClave: string) => api.post<Mensaje>('/api/auth/restablecer', { token, nuevaClave }),
};

export const clientesService = {
  buscar: (q: string, pagina: number, tamano: number, orden: string) =>
    api.get<Pagina<Cliente>>(`/api/clientes${consulta({ q, pagina, tamano, orden })}`),
  detalle: (id: string) => api.get<ClienteDetalle>(`/api/clientes/${id}`),
  crear: (d: ClienteDatos) => api.post<Cliente>('/api/clientes', d),
  actualizar: (id: string, d: ClienteDatos) => api.put<Cliente>(`/api/clientes/${id}`, d),
  desactivar: (id: string) => api.delete(`/api/clientes/${id}`),
  perfil: () => api.get<ClienteDetalle>('/api/clientes/me'),
  actualizarPerfil: (d: { nombre: string; telefono: string; direccion: string }) => api.put<Cliente>('/api/clientes/me', d),
};

export const mascotasService = {
  listar: (propietarioId?: string) => api.get<Mascota[]>(`/api/mascotas${consulta({ propietarioId })}`),
  obtener: (id: string) => api.get<Mascota>(`/api/mascotas/${id}`),
  crear: (d: MascotaDatos) => api.post<Mascota>('/api/mascotas', d),
  actualizar: (id: string, d: MascotaDatos) => api.put<Mascota>(`/api/mascotas/${id}`, d),
  desactivar: (id: string) => api.delete(`/api/mascotas/${id}`),
  historial: (id: string, desde?: string, hasta?: string) =>
    api.get<Atencion[]>(`/api/mascotas/${id}/atenciones${consulta({ desde, hasta })}`),
  registrarAtencion: (id: string, d: AtencionDatos) => api.post<Atencion>(`/api/mascotas/${id}/atenciones`, d),
  actualizarAtencion: (id: string, d: AtencionDatos) => api.put<Atencion>(`/api/atenciones/${id}`, d),
  eliminarAtencion: (id: string) => api.delete(`/api/atenciones/${id}`),
};

export const serviciosService = {
  activos: () => api.get<Servicio[]>('/api/servicios'),
  todos: () => api.get<Servicio[]>('/api/servicios/gestion'),
  crear: (d: ServicioDatos) => api.post<Servicio>('/api/servicios', d),
  actualizar: (id: string, d: ServicioDatos) => api.put<Servicio>(`/api/servicios/${id}`, d),
  cambiarEstado: (id: string, activo: boolean) => api.patch<Servicio>(`/api/servicios/${id}/estado`, { activo }),
};

export const citasService = {
  disponibilidad: (servicioId: string, fecha: string) =>
    api.get<Disponibilidad>(`/api/citas/disponibilidad${consulta({ servicioId, fecha })}`),
  listar: (f: { desde?: string; hasta?: string; estado?: EstadoCita | ''; veterinarioId?: string } = {}) =>
    api.get<Cita[]>(`/api/citas${consulta(f)}`),
  solicitar: (mascotaId: string, servicioId: string, inicio: string) =>
    api.post<Cita>('/api/citas', { mascotaId, servicioId, inicio }),
  confirmar: (id: string) => api.patch<Cita>(`/api/citas/${id}/confirmar`),
  rechazar: (id: string, motivo: string) => api.patch<Cita>(`/api/citas/${id}/rechazar`, { motivo }),
  atender: (id: string) => api.patch<Cita>(`/api/citas/${id}/atender`),
  cancelar: (id: string, motivo: string) => api.patch<Cita>(`/api/citas/${id}/cancelar`, { motivo }),
  reprogramar: (id: string, inicio: string) => api.patch<Cita>(`/api/citas/${id}/reprogramar`, { inicio }),
};

export const productosService = {
  catalogo: (q?: string, categoria?: CategoriaProducto | '') =>
    api.get<Producto[]>(`/api/productos${consulta({ q, categoria })}`),
  gestion: (q?: string, categoria?: CategoriaProducto | '') =>
    api.get<Producto[]>(`/api/productos/gestion${consulta({ q, categoria })}`),
  crear: (d: ProductoDatos) => api.post<Producto>('/api/productos', d),
  actualizar: (id: string, d: ProductoDatos) => api.put<Producto>(`/api/productos/${id}`, d),
  cambiarEstado: (id: string, activo: boolean) => api.patch<Producto>(`/api/productos/${id}/estado`, { activo }),
};

export const inventarioService = {
  registrar: (d: { productoId: string; tipo: TipoMovimiento; cantidad: number; motivo: string; fecha?: string }) =>
    api.post<Movimiento>('/api/inventario/movimientos', d),
  movimientos: (productoId?: string) => api.get<Movimiento[]>(`/api/inventario/movimientos${consulta({ productoId })}`),
  alertas: () => api.get<Producto[]>('/api/inventario/alertas'),
};

export const pedidosService = {
  cotizar: (lineas: LineaCarrito[]) => api.post<Cotizacion>('/api/pedidos/cotizar', { lineas }),
  crear: (lineas: LineaCarrito[], modalidad: ModalidadEntrega, direccionEntrega: string, clienteId?: string) =>
    api.post<Pedido>('/api/pedidos', { lineas, modalidad, direccionEntrega, clienteId }),
  listar: (estado?: EstadoPedido | '') => api.get<Pedido[]>(`/api/pedidos${consulta({ estado })}`),
  cancelar: (id: string, motivo: string) => api.patch<Pedido>(`/api/pedidos/${id}/cancelar`, { motivo }),
  cambiarEstado: (id: string, estado: EstadoPedido) => api.patch<Pedido>(`/api/pedidos/${id}/estado`, { estado }),
};

export const usuariosService = {
  listar: (rol?: Rol | '') => api.get<Usuario[]>(`/api/usuarios${consulta({ rol })}`),
  personal: () => api.get<Usuario[]>('/api/usuarios/personal'),
  crearPersonal: (d: { nombre: string; email: string; clave: string; rol: Rol }) => api.post<Usuario>('/api/usuarios', d),
  cambiarRol: (id: string, rol: Rol) => api.patch<Usuario>(`/api/usuarios/${id}/rol`, { rol }),
  cambiarEstado: (id: string, activo: boolean) => api.patch<Usuario>(`/api/usuarios/${id}/estado`, { activo }),
};

export const panelService = {
  resumen: () => api.get<Panel>('/api/panel'),
};

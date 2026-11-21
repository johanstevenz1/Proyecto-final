// Contratos de datos de la API REST de Jovan Pets (reflejan los DTO del backend).

export type Rol = 'CLIENTE' | 'VETERINARIO' | 'ADMINISTRADOR';
export type Especie = 'PERRO' | 'GATO' | 'AVE' | 'OTRO';
export type Sexo = 'MACHO' | 'HEMBRA';
export type TipoAtencion = 'CONSULTA' | 'CONTROL' | 'CIRUGIA' | 'VACUNA' | 'DESPARASITACION' | 'PELUQUERIA' | 'OTRO';
export type CategoriaServicio = 'VETERINARIA' | 'PELUQUERIA';
export type EstadoCita = 'PENDIENTE' | 'CONFIRMADA' | 'RECHAZADA' | 'CANCELADA' | 'ATENDIDA';
export type CategoriaProducto = 'MEDICAMENTOS' | 'ALIMENTOS' | 'ACCESORIOS' | 'HIGIENE' | 'JUGUETES';
export type TipoMovimiento = 'ENTRADA' | 'SALIDA';
export type ModalidadEntrega = 'DOMICILIO' | 'RECOGIDA';
export type EstadoPedido = 'PENDIENTE' | 'CONFIRMADO' | 'DESPACHADO' | 'ENTREGADO' | 'CANCELADO';

export interface Usuario {
  id: string;
  email: string;
  nombre: string;
  rol: Rol;
  activo: boolean;
  clienteId: string | null;
  fechaRegistro: string | null;
}

export interface Sesion {
  token: string;
  expiraEn: string;
  usuario: Usuario;
}

export interface RegistroDatos {
  nombre: string;
  cedula: string;
  telefono: string;
  email: string;
  direccion: string;
  clave: string;
}

export interface Cliente {
  id: string;
  nombre: string;
  cedula: string;
  telefono: string;
  email: string;
  direccion: string;
  activo: boolean;
  fechaRegistro: string;
}

export type ClienteDatos = Omit<Cliente, 'id' | 'activo' | 'fechaRegistro'>;

export interface CambioCliente {
  fecha: string;
  usuario: string;
  detalle: string;
}

export interface ClienteDetalle {
  cliente: Cliente;
  historial: CambioCliente[];
  mascotas: Mascota[];
}

export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface Mascota {
  id: string;
  propietarioId: string;
  propietarioNombre: string | null;
  nombre: string;
  especie: Especie;
  raza: string;
  sexo: Sexo;
  fechaNacimiento: string;
  peso: number;
  activo: boolean;
}

export interface MascotaDatos {
  propietarioId?: string;
  nombre: string;
  especie: Especie;
  raza: string;
  sexo: Sexo;
  fechaNacimiento: string;
  peso: number;
}

export interface Atencion {
  id: string;
  mascotaId: string;
  tipo: TipoAtencion;
  fecha: string;
  descripcion: string;
  observaciones: string | null;
  profesional: string | null;
  proximaFecha: string | null;
}

export interface AtencionDatos {
  tipo: TipoAtencion;
  fecha: string;
  descripcion: string;
  observaciones?: string;
  proximaFecha?: string | null;
}

export interface Servicio {
  id: string;
  nombre: string;
  descripcion: string;
  categoria: CategoriaServicio;
  precio: number;
  duracionMinutos: number;
  activo: boolean;
}

export type ServicioDatos = Omit<Servicio, 'id'>;

export interface Cita {
  id: string;
  mascotaId: string;
  mascotaNombre: string;
  clienteId: string;
  clienteNombre: string;
  servicioId: string;
  servicioNombre: string;
  agenda: CategoriaServicio;
  inicio: string;
  fin: string;
  estado: EstadoCita;
  motivo: string | null;
  veterinarioId: string | null;
  veterinarioNombre: string | null;
}

export interface Disponibilidad {
  servicioId: string;
  fecha: string;
  horarios: string[];
}

export interface Producto {
  id: string;
  nombre: string;
  descripcion: string;
  categoria: CategoriaProducto;
  precio: number;
  stock: number;
  stockMinimo: number;
  stockBajo: boolean;
  requiereFormula: boolean;
  activo: boolean;
}

export interface ProductoDatos {
  nombre: string;
  descripcion: string;
  categoria: CategoriaProducto;
  precio: number;
  stockInicial?: number;
  stockMinimo: number;
  requiereFormula: boolean;
}

export interface Movimiento {
  id: string;
  productoId: string;
  productoNombre: string;
  fecha: string;
  tipo: TipoMovimiento;
  cantidad: number;
  motivo: string;
  saldo: number;
  usuarioNombre: string;
  pedidoId: string | null;
}

export interface LineaCarrito {
  productoId: string;
  cantidad: number;
}

export interface DetallePedido {
  productoId: string;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Cotizacion {
  detalles: DetallePedido[];
  total: number;
}

export interface Pedido {
  id: string;
  numero: string;
  clienteId: string;
  clienteNombre: string;
  fecha: string;
  estado: EstadoPedido;
  modalidad: ModalidadEntrega;
  direccionEntrega: string | null;
  detalles: DetallePedido[];
  total: number;
  motivoCancelacion: string | null;
}

export interface Panel {
  totalClientes: number;
  totalMascotas: number;
  citasHoy: Partial<Record<EstadoCita, number>>;
  citasPendientes: number;
  alertasStock: Producto[];
  pedidosPendientes: number;
  pedidosRecientes: Pedido[];
}

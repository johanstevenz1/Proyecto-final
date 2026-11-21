// Formatos de presentacion y etiquetas legibles de los valores del dominio.

const moneda = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

export const formatoMoneda = (v: number) => moneda.format(v);

/** Interpreta "YYYY-MM-DD" o "YYYY-MM-DDTHH:mm[:ss]" como fecha local (sin zona horaria). */
function aFecha(texto: string): Date {
  const [f, h = '00:00'] = texto.split('T');
  const [a, m, d] = f.split('-').map(Number);
  const [hh, mm] = h.split(':').map(Number);
  return new Date(a, m - 1, d, hh, mm);
}

export const formatoFecha = (texto?: string | null) =>
  texto ? aFecha(texto).toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' }) : '—';

export const formatoHora = (texto: string) => texto.split('T')[1]?.slice(0, 5) ?? '';

export const formatoFechaHora = (texto?: string | null) => (texto ? `${formatoFecha(texto)} ${formatoHora(texto)}` : '—');

export const formatoInstante = (texto?: string | null) =>
  texto ? new Date(texto).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' }) : '—';

/** Fecha local en formato ISO (YYYY-MM-DD). */
export function hoyIso(desplazamientoDias = 0): string {
  const d = new Date();
  d.setDate(d.getDate() + desplazamientoDias);
  return fechaIso(d);
}

export function fechaIso(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export function sumarDias(iso: string, dias: number): string {
  const d = aFecha(iso);
  d.setDate(d.getDate() + dias);
  return fechaIso(d);
}

export function edad(fechaNacimiento: string): string {
  const n = aFecha(fechaNacimiento);
  const hoy = new Date();
  let meses = (hoy.getFullYear() - n.getFullYear()) * 12 + hoy.getMonth() - n.getMonth();
  if (hoy.getDate() < n.getDate()) meses -= 1;
  if (meses < 12) return `${Math.max(meses, 0)} meses`;
  const anios = Math.floor(meses / 12);
  return `${anios} ${anios === 1 ? 'año' : 'años'}`;
}

export const etiquetas: Record<string, string> = {
  CLIENTE: 'Cliente',
  VETERINARIO: 'Personal veterinario',
  ADMINISTRADOR: 'Administrador',
  PERRO: 'Perro',
  GATO: 'Gato',
  AVE: 'Ave',
  OTRO: 'Otro',
  MACHO: 'Macho',
  HEMBRA: 'Hembra',
  CONSULTA: 'Consulta',
  CONTROL: 'Control',
  CIRUGIA: 'Cirugía',
  VACUNA: 'Vacuna',
  DESPARASITACION: 'Desparasitación',
  PELUQUERIA: 'Peluquería',
  VETERINARIA: 'Veterinaria',
  PENDIENTE: 'Pendiente',
  CONFIRMADA: 'Confirmada',
  RECHAZADA: 'Rechazada',
  CANCELADA: 'Cancelada',
  ATENDIDA: 'Atendida',
  MEDICAMENTOS: 'Medicamentos',
  ALIMENTOS: 'Alimentos',
  ACCESORIOS: 'Accesorios',
  HIGIENE: 'Higiene',
  JUGUETES: 'Juguetes',
  ENTRADA: 'Entrada',
  SALIDA: 'Salida',
  DOMICILIO: 'Entrega a domicilio',
  RECOGIDA: 'Recogida en el establecimiento',
  CONFIRMADO: 'Confirmado',
  DESPACHADO: 'Despachado',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
};

export const etiqueta = (v?: string | null) => (v ? etiquetas[v] ?? v : '—');

/** Fecha y hora local actual como "YYYY-MM-DDTHH:mm", comparable con los LocalDateTime de la API. */
export function ahoraIsoLocal(): string {
  const d = new Date();
  return `${fechaIso(d)}T${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

import type { Sesion } from '../interfaces/tipos';

// URL del backend configurada por variable de entorno (VITE_API_URL).
export const API_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/\/$/, '');

const CLAVE_SESION = 'jovanpets.sesion';
const MARGEN_RENOVACION_MS = 10 * 60 * 1000;

/** Error devuelto por la API con su mensaje y los errores por campo. */
export class ApiError extends Error {
  constructor(
    public estado: number,
    mensaje: string,
    public errores: Record<string, string> = {},
  ) {
    super(mensaje);
  }
}

let sesion: Sesion | null = leerSesionGuardada();
let renovando: Promise<void> | null = null;
let alExpirar: (() => void) | null = null;
const oyentes = new Set<(s: Sesion | null) => void>();

function almacen(): Storage {
  return localStorage.getItem(CLAVE_SESION) ? localStorage : sessionStorage;
}

function leerSesionGuardada(): Sesion | null {
  try {
    const texto = sessionStorage.getItem(CLAVE_SESION) ?? localStorage.getItem(CLAVE_SESION);
    if (!texto) return null;
    const s = JSON.parse(texto) as Sesion;
    return new Date(s.expiraEn).getTime() > Date.now() ? s : null;
  } catch {
    return null;
  }
}

export function sesionActual(): Sesion | null {
  return sesion;
}

export function guardarSesion(nueva: Sesion | null, recordar?: boolean): void {
  sesion = nueva;
  try {
    if (!nueva) {
      sessionStorage.removeItem(CLAVE_SESION);
      localStorage.removeItem(CLAVE_SESION);
    } else {
      const destino = recordar === undefined ? almacen() : recordar ? localStorage : sessionStorage;
      (destino === localStorage ? sessionStorage : localStorage).removeItem(CLAVE_SESION);
      destino.setItem(CLAVE_SESION, JSON.stringify(nueva));
    }
  } catch {
    // Almacenamiento no disponible (modo privado): la sesion vive solo en memoria.
  }
  oyentes.forEach((f) => f(nueva));
}

export function suscribirSesion(f: (s: Sesion | null) => void): () => void {
  oyentes.add(f);
  return () => oyentes.delete(f);
}

export function alExpirarSesion(f: () => void): void {
  alExpirar = f;
}

/** Renueva el token mientras el usuario sigue activo (la sesion expira por inactividad). */
async function renovarSiHaceFalta(ruta: string): Promise<void> {
  if (!sesion || ruta.startsWith('/api/auth/')) return;
  const restante = new Date(sesion.expiraEn).getTime() - Date.now();
  if (restante > MARGEN_RENOVACION_MS || restante <= 0) return;
  if (!renovando) {
    renovando = fetch(`${API_URL}/api/auth/renovar`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${sesion.token}` },
    })
      .then(async (r) => {
        if (r.ok) guardarSesion((await r.json()) as Sesion);
      })
      .catch(() => undefined)
      .finally(() => {
        renovando = null;
      });
  }
  await renovando;
}

async function solicitud<T>(metodo: string, ruta: string, cuerpo?: unknown): Promise<T> {
  await renovarSiHaceFalta(ruta);
  const cabeceras: Record<string, string> = {};
  if (cuerpo !== undefined) cabeceras['Content-Type'] = 'application/json';
  if (sesion) cabeceras.Authorization = `Bearer ${sesion.token}`;

  let respuesta: Response;
  try {
    respuesta = await fetch(`${API_URL}${ruta}`, {
      method: metodo,
      headers: cabeceras,
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ApiError(0, 'No se pudo conectar con el servidor. Verifica que el backend este en ejecucion.');
  }

  if (respuesta.status === 204) return undefined as T;
  const datos = await respuesta.json().catch(() => null);
  if (!respuesta.ok) {
    if (respuesta.status === 401 && sesion && !ruta.startsWith('/api/auth/login')) {
      guardarSesion(null);
      alExpirar?.();
    }
    throw new ApiError(respuesta.status, datos?.mensaje ?? 'Ocurrio un error inesperado', datos?.errores ?? {});
  }
  return datos as T;
}

/** Construye la cadena de consulta omitiendo valores vacios. */
export function consulta(parametros: Record<string, string | number | undefined | null>): string {
  const p = new URLSearchParams();
  Object.entries(parametros).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') p.set(k, String(v));
  });
  const texto = p.toString();
  return texto ? `?${texto}` : '';
}

export const api = {
  get: <T>(ruta: string) => solicitud<T>('GET', ruta),
  post: <T>(ruta: string, cuerpo?: unknown) => solicitud<T>('POST', ruta, cuerpo ?? {}),
  put: <T>(ruta: string, cuerpo: unknown) => solicitud<T>('PUT', ruta, cuerpo),
  patch: <T>(ruta: string, cuerpo?: unknown) => solicitud<T>('PATCH', ruta, cuerpo ?? {}),
  delete: <T = void>(ruta: string) => solicitud<T>('DELETE', ruta),
};

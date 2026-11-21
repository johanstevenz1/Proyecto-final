import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '../services/api';

/** Ejecuta una consulta a la API y expone sus estados de carga, error y datos. */
export function useCarga<T>(consulta: () => Promise<T>, dependencias: unknown[] = []) {
  const [datos, setDatos] = useState<T | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const ultima = useRef(0);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const ejecutar = useCallback(consulta, dependencias);

  const recargar = useCallback(async () => {
    const id = ++ultima.current;
    setCargando(true);
    setError(null);
    try {
      const r = await ejecutar();
      if (id === ultima.current) setDatos(r);
    } catch (e) {
      if (id === ultima.current) setError(mensajeDeError(e));
    } finally {
      if (id === ultima.current) setCargando(false);
    }
  }, [ejecutar]);

  useEffect(() => {
    void recargar();
  }, [recargar]);

  return { datos, setDatos, cargando, error, recargar };
}

/** Mensaje legible de un error de la API, incluyendo los errores de validacion por campo. */
export function mensajeDeError(e: unknown): string {
  if (e instanceof ApiError) {
    const detalles = Object.values(e.errores ?? {});
    return detalles.length ? `${e.message}: ${detalles.join('. ')}` : e.message;
  }
  return e instanceof Error ? e.message : 'Ocurrio un error inesperado';
}

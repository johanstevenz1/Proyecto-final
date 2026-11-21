import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { RegistroDatos, Rol, Sesion, Usuario } from '../interfaces/tipos';
import { alExpirarSesion, guardarSesion, sesionActual, suscribirSesion } from '../services/api';
import { authService } from '../services/servicios';

interface AuthValor {
  usuario: Usuario | null;
  avisoSesion: string | null;
  iniciarSesion: (email: string, clave: string, recordar: boolean) => Promise<Usuario>;
  registrar: (d: RegistroDatos) => Promise<Usuario>;
  cerrarSesion: () => Promise<void>;
  actualizarSesion: (s: Sesion) => void;
  tieneRol: (...roles: Rol[]) => boolean;
  limpiarAviso: () => void;
}

const AuthContext = createContext<AuthValor | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sesion, setSesion] = useState<Sesion | null>(sesionActual());
  const [avisoSesion, setAvisoSesion] = useState<string | null>(null);

  useEffect(() => {
    const quitar = suscribirSesion(setSesion);
    alExpirarSesion(() => setAvisoSesion('Tu sesion expiro. Inicia sesion de nuevo.'));
    return () => {
      quitar();
    };
  }, []);

  // Cierra la sesion local cuando el token vence por inactividad (RNF-04).
  useEffect(() => {
    if (!sesion) return;
    const restante = new Date(sesion.expiraEn).getTime() - Date.now();
    const temporizador = window.setTimeout(() => {
      guardarSesion(null);
      setAvisoSesion('Tu sesion expiro por inactividad. Inicia sesion de nuevo.');
    }, Math.max(restante, 0));
    return () => window.clearTimeout(temporizador);
  }, [sesion]);

  // Sincroniza el rol vigente al cargar (puede haber cambiado, HU-20).
  useEffect(() => {
    if (sesionActual()) {
      authService
        .yo()
        .then((u) => {
          const s = sesionActual();
          if (s) guardarSesion({ ...s, usuario: u });
        })
        .catch(() => undefined);
    }
  }, []);

  const iniciarSesion = useCallback(async (email: string, clave: string, recordar: boolean) => {
    const s = await authService.login(email, clave);
    guardarSesion(s, recordar);
    setAvisoSesion(null);
    return s.usuario;
  }, []);

  const registrar = useCallback(async (d: RegistroDatos) => {
    const s = await authService.registrar(d);
    guardarSesion(s, false);
    return s.usuario;
  }, []);

  const cerrarSesion = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      guardarSesion(null);
    }
  }, []);

  const valor = useMemo<AuthValor>(
    () => ({
      usuario: sesion?.usuario ?? null,
      avisoSesion,
      iniciarSesion,
      registrar,
      cerrarSesion,
      actualizarSesion: (s) => guardarSesion(s),
      tieneRol: (...roles) => !!sesion && roles.includes(sesion.usuario.rol),
      limpiarAviso: () => setAvisoSesion(null),
    }),
    [sesion, avisoSesion, iniciarSesion, registrar, cerrarSesion],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthValor {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return ctx;
}

/** Ruta inicial segun el rol (HU-19: redirigir al dashboard correspondiente). */
export function inicioPorRol(rol: Rol): string {
  return rol === 'CLIENTE' ? '/portal' : '/panel';
}

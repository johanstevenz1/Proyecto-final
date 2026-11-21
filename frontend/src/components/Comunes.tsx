import { useState, type ReactNode } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import type { Rol } from '../interfaces/tipos';
import { etiqueta } from '../utils/formato';

/** Mensaje de confirmacion, advertencia o error. */
export function Alerta({ tipo = 'info', children, onCerrar }: { tipo?: 'exito' | 'error' | 'info' | 'aviso'; children: ReactNode; onCerrar?: () => void }) {
  if (!children) return null;
  return (
    <div className={`alerta alerta-${tipo}`} role={tipo === 'error' ? 'alert' : 'status'}>
      <span>{children}</span>
      {onCerrar && (
        <button type="button" className="alerta-cerrar" onClick={onCerrar} aria-label="Cerrar mensaje">
          ×
        </button>
      )}
    </div>
  );
}

export function Cargando({ texto = 'Cargando...' }: { texto?: string }) {
  return (
    <div className="cargando" role="status">
      <span className="spinner" aria-hidden="true" />
      {texto}
    </div>
  );
}

/** Estado de una consulta: carga, error con reintento o contenido. */
export function EstadoCarga({ cargando, error, onReintentar, children }: { cargando: boolean; error: string | null; onReintentar?: () => void; children: ReactNode }) {
  if (cargando) return <Cargando />;
  if (error)
    return (
      <Alerta tipo="error">
        {error}{' '}
        {onReintentar && (
          <button type="button" className="enlace" onClick={onReintentar}>
            Reintentar
          </button>
        )}
      </Alerta>
    );
  return <>{children}</>;
}

/** Encabezado de pagina con migas de pan, como en los wireframes. */
export function Encabezado({ titulo, migas = [], acciones }: { titulo: string; migas?: { texto: string; a?: string }[]; acciones?: ReactNode }) {
  return (
    <div className="encabezado-pagina">
      <nav className="migas" aria-label="Ruta de navegacion">
        {migas.map((m, i) => (
          <span key={i}>
            {m.a ? <Link to={m.a}>{m.texto}</Link> : m.texto}
            {' / '}
          </span>
        ))}
        <span aria-current="page">{titulo}</span>
      </nav>
      <div className="encabezado-fila">
        <h1>{titulo}</h1>
        {acciones && <div className="acciones">{acciones}</div>}
      </div>
    </div>
  );
}

const coloresEstado: Record<string, string> = {
  PENDIENTE: 'naranja',
  CONFIRMADA: 'verde',
  CONFIRMADO: 'azul',
  ATENDIDA: 'gris',
  DESPACHADO: 'morado',
  ENTREGADO: 'verde',
  RECHAZADA: 'rojo',
  CANCELADA: 'rojo',
  CANCELADO: 'rojo',
  ENTRADA: 'verde',
  SALIDA: 'naranja',
};

export function Insignia({ valor, texto }: { valor: string; texto?: string }) {
  return <span className={`insignia insignia-${coloresEstado[valor] ?? 'azul'}`}>{texto ?? etiqueta(valor)}</span>;
}

export function Tarjeta({ titulo, children, acciones, className = '' }: { titulo?: ReactNode; children: ReactNode; acciones?: ReactNode; className?: string }) {
  return (
    <section className={`tarjeta ${className}`}>
      {(titulo || acciones) && (
        <header className="tarjeta-encabezado">
          {titulo && <h2>{titulo}</h2>}
          {acciones}
        </header>
      )}
      {children}
    </section>
  );
}

export function Vacio({ children }: { children: ReactNode }) {
  return <p className="vacio">{children}</p>;
}

/** Campo de formulario con etiqueta, ayuda y mensaje de error. */
export function Campo({ etiqueta: texto, error, ayuda, requerido, children }: { etiqueta: string; error?: string; ayuda?: string; requerido?: boolean; children: ReactNode }) {
  return (
    <label className={`campo ${error ? 'campo-error' : ''}`}>
      <span className="campo-etiqueta">
        {texto}
        {requerido && <span className="requerido"> *</span>}
      </span>
      {children}
      {ayuda && !error && <small className="ayuda">{ayuda}</small>}
      {error && <small className="error-texto">{error}</small>}
    </label>
  );
}

export function Modal({ titulo, children, onCerrar }: { titulo: string; children: ReactNode; onCerrar: () => void }) {
  return (
    <div className="modal-fondo" role="presentation" onClick={onCerrar}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={titulo} onClick={(e) => e.stopPropagation()}>
        <header className="modal-encabezado">
          <h2>{titulo}</h2>
          <button type="button" className="alerta-cerrar" onClick={onCerrar} aria-label="Cerrar">
            ×
          </button>
        </header>
        {children}
      </div>
    </div>
  );
}

/** Solicita un motivo obligatorio antes de rechazar o cancelar (HU-10, HU-23, RPE-05). */
export function ModalMotivo({ titulo, boton, onConfirmar, onCerrar }: { titulo: string; boton: string; onConfirmar: (motivo: string) => Promise<void>; onCerrar: () => void }) {
  const [motivo, setMotivo] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  return (
    <Modal titulo={titulo} onCerrar={onCerrar}>
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          if (!motivo.trim()) {
            setError('El motivo es obligatorio');
            return;
          }
          setEnviando(true);
          try {
            await onConfirmar(motivo.trim());
          } catch (err) {
            setError(err instanceof Error ? err.message : 'No se pudo completar la accion');
            setEnviando(false);
          }
        }}
      >
        <Campo etiqueta="Motivo" requerido error={error ?? undefined}>
          <textarea value={motivo} maxLength={500} rows={3} onChange={(e) => setMotivo(e.target.value)} autoFocus />
        </Campo>
        <div className="acciones-formulario">
          <button type="button" className="boton boton-secundario" onClick={onCerrar}>
            Volver
          </button>
          <button type="submit" className="boton boton-peligro" disabled={enviando}>
            {enviando ? 'Procesando...' : boton}
          </button>
        </div>
      </form>
    </Modal>
  );
}

export function Paginacion({ pagina, totalPaginas, onCambiar }: { pagina: number; totalPaginas: number; onCambiar: (p: number) => void }) {
  if (totalPaginas <= 1) return null;
  return (
    <div className="paginacion">
      <button type="button" className="boton boton-secundario" disabled={pagina === 0} onClick={() => onCambiar(pagina - 1)}>
        Anterior
      </button>
      <span>
        Pagina {pagina + 1} de {totalPaginas}
      </span>
      <button type="button" className="boton boton-secundario" disabled={pagina + 1 >= totalPaginas} onClick={() => onCambiar(pagina + 1)}>
        Siguiente
      </button>
    </div>
  );
}

/** Protege rutas por autenticacion y rol (HU-21). */
export function RutaProtegida({ roles, children }: { roles?: Rol[]; children: ReactNode }) {
  const { usuario } = useAuth();
  const ubicacion = useLocation();
  if (!usuario) return <Navigate to="/login" replace state={{ desde: ubicacion.pathname }} />;
  if (roles && !roles.includes(usuario.rol)) return <Navigate to="/acceso-denegado" replace />;
  return <>{children}</>;
}

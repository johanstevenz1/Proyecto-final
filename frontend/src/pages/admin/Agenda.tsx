import { useState } from 'react';
import { Alerta, Campo, Encabezado, EstadoCarga, Insignia, ModalMotivo, Tarjeta, Vacio } from '../../components/Comunes';
import { mensajeDeError, useCarga } from '../../hooks/useCarga';
import type { Cita, EstadoCita } from '../../interfaces/tipos';
import { citasService, usuariosService } from '../../services/servicios';
import { etiqueta, formatoHora, hoyIso, sumarDias } from '../../utils/formato';
import { ModalReprogramar, TablaCitas } from '../cliente/PortalCliente';

type Vista = 'pendientes' | 'dia' | 'semana';

/** Lunes de la semana que contiene la fecha indicada. */
function lunes(iso: string): string {
  const [a, m, d] = iso.split('-').map(Number);
  const dia = new Date(a, m - 1, d).getDay();
  return sumarDias(iso, dia === 0 ? -6 : 1 - dia);
}

/**
 * Agenda del personal: solicitudes pendientes (HU-10) y calendario diario/semanal con filtro por
 * veterinario, diferenciando confirmadas y pendientes (HU-11).
 */
export function AgendaCitas() {
  const [vista, setVista] = useState<Vista>('pendientes');
  const [fecha, setFecha] = useState(hoyIso());
  const [veterinarioId, setVeterinarioId] = useState('');
  const [estado, setEstado] = useState<EstadoCita | ''>('');
  const [rechazar, setRechazar] = useState<Cita | null>(null);
  const [cancelar, setCancelar] = useState<Cita | null>(null);
  const [reprogramar, setReprogramar] = useState<Cita | null>(null);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const desde = vista === 'semana' ? lunes(fecha) : fecha;
  const hasta = vista === 'semana' ? sumarDias(desde, 6) : fecha;
  const personal = useCarga(() => usuariosService.personal(), []);
  const citas = useCarga(
    () =>
      vista === 'pendientes'
        ? citasService.listar({ estado: 'PENDIENTE' })
        : citasService.listar({ desde, hasta, estado, veterinarioId }),
    [vista, desde, hasta, estado, veterinarioId],
  );

  async function accion(f: () => Promise<unknown>, texto: string) {
    setError(null);
    try {
      await f();
      setMensaje(texto);
      await citas.recargar();
    } catch (e) {
      setError(mensajeDeError(e));
    }
  }

  const acciones = (c: Cita) => (
    <>
      {c.estado === 'PENDIENTE' && (
        <>
          <button className="boton boton-exito boton-pequeno" type="button" onClick={() => accion(() => citasService.confirmar(c.id), 'Cita confirmada.')}>
            Confirmar
          </button>
          <button className="boton boton-peligro boton-pequeno" type="button" onClick={() => setRechazar(c)}>
            Rechazar
          </button>
        </>
      )}
      {c.estado === 'CONFIRMADA' && (
        <button className="boton boton-pequeno" type="button" onClick={() => accion(() => citasService.atender(c.id), 'Cita marcada como atendida.')}>
          Atendida
        </button>
      )}
      {(c.estado === 'PENDIENTE' || c.estado === 'CONFIRMADA') && (
        <>
          <button className="boton boton-secundario boton-pequeno" type="button" onClick={() => setReprogramar(c)}>
            Reprogramar
          </button>
          <button className="boton boton-secundario boton-pequeno" type="button" onClick={() => setCancelar(c)}>
            Cancelar
          </button>
        </>
      )}
    </>
  );

  const dias = Array.from({ length: 7 }, (_, i) => sumarDias(desde, i));

  return (
    <>
      <Encabezado titulo="Agenda de citas" migas={[{ texto: 'Panel', a: '/panel' }]} />
      {mensaje && (
        <Alerta tipo="exito" onCerrar={() => setMensaje(null)}>
          {mensaje}
        </Alerta>
      )}
      {error && (
        <Alerta tipo="error" onCerrar={() => setError(null)}>
          {error}
        </Alerta>
      )}
      <Tarjeta>
        <div className="pestanas" role="tablist">
          {(['pendientes', 'dia', 'semana'] as Vista[]).map((v) => (
            <button key={v} type="button" role="tab" aria-selected={vista === v} className={vista === v ? 'activa' : ''} onClick={() => setVista(v)}>
              {v === 'pendientes' ? 'Solicitudes pendientes' : v === 'dia' ? 'Calendario diario' : 'Calendario semanal'}
            </button>
          ))}
        </div>
        {vista !== 'pendientes' && (
          <div className="filtros">
            <button className="boton boton-secundario" type="button" onClick={() => setFecha(sumarDias(fecha, vista === 'semana' ? -7 : -1))}>
              ◀ Anterior
            </button>
            <Campo etiqueta={vista === 'semana' ? 'Semana del' : 'Fecha'}>
              <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value || hoyIso())} />
            </Campo>
            <button className="boton boton-secundario" type="button" onClick={() => setFecha(sumarDias(fecha, vista === 'semana' ? 7 : 1))}>
              Siguiente ▶
            </button>
            <Campo etiqueta="Veterinario">
              <select value={veterinarioId} onChange={(e) => setVeterinarioId(e.target.value)}>
                <option value="">Todos</option>
                {personal.datos?.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nombre}
                  </option>
                ))}
              </select>
            </Campo>
            <Campo etiqueta="Estado">
              <select value={estado} onChange={(e) => setEstado(e.target.value as EstadoCita | '')}>
                <option value="">Todos</option>
                {(['PENDIENTE', 'CONFIRMADA', 'ATENDIDA', 'CANCELADA', 'RECHAZADA'] as EstadoCita[]).map((x) => (
                  <option key={x} value={x}>
                    {etiqueta(x)}
                  </option>
                ))}
              </select>
            </Campo>
          </div>
        )}
        {vista !== 'pendientes' && (
          <div className="leyenda">
            <span>
              <Insignia valor="CONFIRMADA" /> borde verde continuo
            </span>
            <span>
              <Insignia valor="PENDIENTE" /> borde naranja punteado
            </span>
          </div>
        )}
        <EstadoCarga cargando={citas.cargando} error={citas.error} onReintentar={citas.recargar}>
          {vista === 'semana' ? (
            <div className="semana">
              {dias.map((d) => {
                const delDia = (citas.datos ?? []).filter((c) => c.inicio.startsWith(d));
                const [a, m, dd] = d.split('-').map(Number);
                return (
                  <div key={d} className={`dia-columna ${d === hoyIso() ? 'hoy' : ''}`}>
                    <h3>{new Date(a, m - 1, dd).toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'short' })}</h3>
                    {delDia.length ? (
                      delDia.map((c) => (
                        <div key={c.id} className={`bloque-cita ${c.estado}`} title={`${etiqueta(c.estado)} · ${c.veterinarioNombre ?? 'Sin asignar'}`}>
                          <strong>{formatoHora(c.inicio)}</strong> {c.servicioNombre}
                          <br />
                          {c.mascotaNombre} · {c.clienteNombre}
                        </div>
                      ))
                    ) : (
                      <small className="ayuda">Sin citas</small>
                    )}
                  </div>
                );
              })}
            </div>
          ) : vista === 'dia' ? (
            citas.datos?.length ? (
              <div>
                {citas.datos.map((c) => (
                  <div key={c.id} className={`bloque-cita ${c.estado}`} style={{ fontSize: '0.92rem', display: 'flex', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
                    <span>
                      <strong>
                        {formatoHora(c.inicio)} - {formatoHora(c.fin)}
                      </strong>{' '}
                      · {c.clienteNombre} · {c.mascotaNombre} · {c.servicioNombre} <Insignia valor={c.estado} />
                      {c.veterinarioNombre && <small> · {c.veterinarioNombre}</small>}
                    </span>
                    <span className="celda-acciones">{acciones(c)}</span>
                  </div>
                ))}
              </div>
            ) : (
              <Vacio>No hay citas para este día.</Vacio>
            )
          ) : (
            <TablaCitas citas={citas.datos ?? []} acciones={acciones} mostrarCliente />
          )}
        </EstadoCarga>
      </Tarjeta>
      {rechazar && (
        <ModalMotivo
          titulo={`Rechazar cita de ${rechazar.mascotaNombre}`}
          boton="Rechazar cita"
          onCerrar={() => setRechazar(null)}
          onConfirmar={async (motivo) => {
            await citasService.rechazar(rechazar.id, motivo);
            setRechazar(null);
            setMensaje('Cita rechazada; el horario quedó libre.');
            await citas.recargar();
          }}
        />
      )}
      {cancelar && (
        <ModalMotivo
          titulo={`Cancelar cita de ${cancelar.mascotaNombre}`}
          boton="Cancelar cita"
          onCerrar={() => setCancelar(null)}
          onConfirmar={async (motivo) => {
            await citasService.cancelar(cancelar.id, motivo);
            setCancelar(null);
            setMensaje('Cita cancelada.');
            await citas.recargar();
          }}
        />
      )}
      {reprogramar && (
        <ModalReprogramar
          cita={reprogramar}
          onCerrar={() => setReprogramar(null)}
          onListo={async () => {
            setReprogramar(null);
            setMensaje('Cita reprogramada.');
            await citas.recargar();
          }}
        />
      )}
    </>
  );
}

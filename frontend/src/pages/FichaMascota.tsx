import { useState, type FormEvent, type ReactNode } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Alerta, Campo, Encabezado, EstadoCarga, Modal, Tarjeta, Vacio } from '../components/Comunes';
import { FormularioMascota } from '../components/Formularios';
import { useAuth } from '../contexts/AuthContext';
import { mensajeDeError, useCarga } from '../hooks/useCarga';
import type { Atencion, AtencionDatos, TipoAtencion } from '../interfaces/tipos';
import { mascotasService } from '../services/servicios';
import { edad, etiqueta, formatoFecha, hoyIso } from '../utils/formato';

const TIPOS: TipoAtencion[] = ['CONSULTA', 'CONTROL', 'CIRUGIA', 'VACUNA', 'DESPARASITACION', 'PELUQUERIA', 'OTRO'];

/**
 * Ficha de la mascota con historial clinico (HU-06, HU-07) y control preventivo (HU-08). El
 * cliente solo consulta; el personal registra, edita y elimina entradas.
 */
export function FichaMascota() {
  const { id = '' } = useParams();
  const { usuario } = useAuth();
  const navegar = useNavigate();
  const esPersonal = usuario?.rol !== 'CLIENTE';
  const base = esPersonal ? '/panel/mascotas' : '/portal/mascotas';
  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');
  const mascota = useCarga(() => mascotasService.obtener(id), [id]);
  const historial = useCarga(() => mascotasService.historial(id, desde || undefined, hasta || undefined), [id, desde, hasta]);
  const [editar, setEditar] = useState(false);
  const [atencion, setAtencion] = useState<Atencion | 'nueva' | null>(null);
  const [tipoNuevo, setTipoNuevo] = useState<TipoAtencion>('CONSULTA');
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const entradas = historial.datos ?? [];
  const clinicas = entradas.filter((a) => a.tipo !== 'VACUNA' && a.tipo !== 'DESPARASITACION');
  const vacunas = entradas.filter((a) => a.tipo === 'VACUNA');
  const desparasitaciones = entradas.filter((a) => a.tipo === 'DESPARASITACION');

  async function eliminar(a: Atencion) {
    if (!window.confirm(`¿Eliminar la entrada "${a.descripcion}" del ${formatoFecha(a.fecha)}?`)) return;
    try {
      await mascotasService.eliminarAtencion(a.id);
      setMensaje('Entrada eliminada.');
      await historial.recargar();
    } catch (e) {
      setError(mensajeDeError(e));
    }
  }

  const accionesEntrada = (a: Atencion) =>
    esPersonal ? (
      <span className="celda-acciones">
        <button className="boton boton-secundario boton-pequeno" type="button" onClick={() => setAtencion(a)}>
          Editar
        </button>
        <button className="boton boton-peligro boton-pequeno" type="button" onClick={() => eliminar(a)}>
          Eliminar
        </button>
      </span>
    ) : null;

  const nueva = (tipo: TipoAtencion) => {
    setTipoNuevo(tipo);
    setAtencion('nueva');
  };

  return (
    <EstadoCarga cargando={mascota.cargando} error={mascota.error} onReintentar={mascota.recargar}>
      {mascota.datos && (
        <>
          <Encabezado
            titulo={`Ficha de ${mascota.datos.nombre}`}
            migas={[{ texto: esPersonal ? 'Panel' : 'Portal', a: esPersonal ? '/panel' : '/portal' }, { texto: 'Mascotas', a: base }]}
            acciones={
              <>
                <button className="boton boton-secundario" type="button" onClick={() => setEditar(true)}>
                  Editar datos
                </button>
                {usuario?.rol !== 'VETERINARIO' && (
                  <button
                    className="boton boton-peligro"
                    type="button"
                    onClick={async () => {
                      if (!window.confirm('¿Desactivar esta mascota? Dejará de aparecer en los listados.')) return;
                      try {
                        await mascotasService.desactivar(id);
                        navegar(base);
                      } catch (e) {
                        setError(mensajeDeError(e));
                      }
                    }}
                  >
                    Desactivar
                  </button>
                )}
              </>
            }
          />
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
          <Tarjeta titulo="Datos de la mascota">
            <div className="rejilla rejilla-4">
              <Dato titulo="Especie" valor={etiqueta(mascota.datos.especie)} />
              <Dato titulo="Raza" valor={mascota.datos.raza} />
              <Dato titulo="Sexo" valor={etiqueta(mascota.datos.sexo)} />
              <Dato titulo="Edad" valor={`${edad(mascota.datos.fechaNacimiento)} (${formatoFecha(mascota.datos.fechaNacimiento)})`} />
              <Dato titulo="Peso" valor={`${mascota.datos.peso} kg`} />
              <Dato titulo="Propietario" valor={mascota.datos.propietarioNombre ?? '—'} />
            </div>
          </Tarjeta>

          <div className="rejilla rejilla-2">
            <Tarjeta titulo="Vacunas" acciones={esPersonal && <button className="boton boton-pequeno" type="button" onClick={() => nueva('VACUNA')}>Registrar vacuna</button>}>
              <TablaPreventiva entradas={vacunas} titulo="Vacuna" acciones={accionesEntrada} />
            </Tarjeta>
            <Tarjeta
              titulo="Desparasitaciones"
              acciones={esPersonal && <button className="boton boton-pequeno" type="button" onClick={() => nueva('DESPARASITACION')}>Registrar desparasitación</button>}
            >
              <TablaPreventiva entradas={desparasitaciones} titulo="Producto" acciones={accionesEntrada} />
            </Tarjeta>
          </div>

          <Tarjeta
            titulo="Historial de controles y atenciones"
            acciones={esPersonal && <button className="boton" type="button" onClick={() => nueva('CONSULTA')}>Registrar antecedente</button>}
          >
            <div className="filtros">
              <Campo etiqueta="Desde">
                <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} />
              </Campo>
              <Campo etiqueta="Hasta">
                <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} />
              </Campo>
              {(desde || hasta) && (
                <button className="boton boton-secundario" type="button" onClick={() => { setDesde(''); setHasta(''); }}>
                  Quitar filtro
                </button>
              )}
            </div>
            <EstadoCarga cargando={historial.cargando} error={historial.error} onReintentar={historial.recargar}>
              {clinicas.length ? (
                <ul className="linea-tiempo">
                  {clinicas.map((a) => (
                    <li key={a.id}>
                      <details>
                        <summary>
                          {formatoFecha(a.fecha)} · {etiqueta(a.tipo)} · {a.descripcion}
                        </summary>
                        <p>
                          <strong>Diagnóstico / descripción:</strong> {a.descripcion}
                        </p>
                        {a.observaciones && (
                          <p>
                            <strong>Observaciones:</strong> {a.observaciones}
                          </p>
                        )}
                        <p>
                          <strong>Profesional:</strong> {a.profesional ?? '—'}
                          {a.proximaFecha && ` · Próximo control: ${formatoFecha(a.proximaFecha)}`}
                        </p>
                        {accionesEntrada(a)}
                      </details>
                    </li>
                  ))}
                </ul>
              ) : (
                <Vacio>No hay entradas en el historial{desde || hasta ? ' para ese rango de fechas' : ''}.</Vacio>
              )}
            </EstadoCarga>
          </Tarjeta>

          {editar && (
            <Modal titulo="Editar mascota" onCerrar={() => setEditar(false)}>
              <FormularioMascota
                inicial={mascota.datos}
                conPropietario={esPersonal}
                onCancelar={() => setEditar(false)}
                onGuardar={async (d) => {
                  await mascotasService.actualizar(id, d);
                  setEditar(false);
                  setMensaje('Datos actualizados.');
                  await mascota.recargar();
                }}
              />
            </Modal>
          )}
          {atencion && (
            <Modal titulo={atencion === 'nueva' ? 'Registrar entrada' : 'Editar entrada'} onCerrar={() => setAtencion(null)}>
              <FormularioAtencion
                inicial={atencion === 'nueva' ? undefined : atencion}
                tipoInicial={tipoNuevo}
                onCancelar={() => setAtencion(null)}
                onGuardar={async (d) => {
                  if (atencion === 'nueva') await mascotasService.registrarAtencion(id, d);
                  else await mascotasService.actualizarAtencion(atencion.id, d);
                  setAtencion(null);
                  setMensaje('Historial actualizado.');
                  await historial.recargar();
                }}
              />
            </Modal>
          )}
        </>
      )}
    </EstadoCarga>
  );
}

function Dato({ titulo, valor }: { titulo: string; valor: string }) {
  return (
    <div>
      <div className="campo-etiqueta" style={{ color: 'var(--texto-suave)' }}>
        {titulo}
      </div>
      <div>{valor}</div>
    </div>
  );
}

function TablaPreventiva({ entradas, titulo, acciones }: { entradas: Atencion[]; titulo: string; acciones: (a: Atencion) => ReactNode }) {
  if (!entradas.length) return <Vacio>Sin registros.</Vacio>;
  return (
    <div className="tabla-contenedor">
      <table>
        <thead>
          <tr>
            <th>{titulo}</th>
            <th>Aplicación</th>
            <th>Próxima dosis</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {entradas.map((a) => (
            <tr key={a.id}>
              <td>{a.descripcion}</td>
              <td>{formatoFecha(a.fecha)}</td>
              <td>
                <strong>{formatoFecha(a.proximaFecha)}</strong>
              </td>
              <td>{acciones(a)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function FormularioAtencion({
  inicial,
  tipoInicial,
  onGuardar,
  onCancelar,
}: {
  inicial?: Atencion;
  tipoInicial: TipoAtencion;
  onGuardar: (d: AtencionDatos) => Promise<void>;
  onCancelar: () => void;
}) {
  const [d, setD] = useState<AtencionDatos>({
    tipo: inicial?.tipo ?? tipoInicial,
    fecha: inicial?.fecha ?? hoyIso(),
    descripcion: inicial?.descripcion ?? '',
    observaciones: inicial?.observaciones ?? '',
    proximaFecha: inicial?.proximaFecha ?? '',
  });
  const [error, setError] = useState<string | null>(null);
  const preventiva = d.tipo === 'VACUNA' || d.tipo === 'DESPARASITACION';

  async function enviar(e: FormEvent) {
    e.preventDefault();
    if (!d.descripcion.trim()) return setError('La descripción es obligatoria');
    if (d.fecha > hoyIso()) return setError('La fecha no puede ser futura');
    if (d.proximaFecha && d.proximaFecha <= d.fecha) return setError('La próxima fecha debe ser posterior');
    try {
      await onGuardar({ ...d, proximaFecha: d.proximaFecha || null });
    } catch (err) {
      setError(mensajeDeError(err));
    }
  }

  return (
    <form className="formulario" onSubmit={enviar} noValidate>
      {error && (
        <div className="completo">
          <Alerta tipo="error">{error}</Alerta>
        </div>
      )}
      <Campo etiqueta="Tipo de atención" requerido>
        <select value={d.tipo} onChange={(e) => setD({ ...d, tipo: e.target.value as TipoAtencion })}>
          {TIPOS.map((t) => (
            <option key={t} value={t}>
              {etiqueta(t)}
            </option>
          ))}
        </select>
      </Campo>
      <Campo etiqueta={preventiva ? 'Fecha de aplicación' : 'Fecha'} requerido>
        <input type="date" max={hoyIso()} value={d.fecha} onChange={(e) => setD({ ...d, fecha: e.target.value })} />
      </Campo>
      <div className="completo">
        <Campo etiqueta={d.tipo === 'VACUNA' ? 'Nombre de la vacuna' : d.tipo === 'DESPARASITACION' ? 'Producto' : 'Diagnóstico'} requerido>
          <input value={d.descripcion} maxLength={500} onChange={(e) => setD({ ...d, descripcion: e.target.value })} />
        </Campo>
      </div>
      <div className="completo">
        <Campo etiqueta="Observaciones">
          <textarea rows={3} maxLength={2000} value={d.observaciones ?? ''} onChange={(e) => setD({ ...d, observaciones: e.target.value })} />
        </Campo>
      </div>
      <Campo etiqueta={preventiva ? 'Próxima dosis' : 'Próximo control'}>
        <input type="date" min={d.fecha} value={d.proximaFecha ?? ''} onChange={(e) => setD({ ...d, proximaFecha: e.target.value })} />
      </Campo>
      <div className="acciones-formulario">
        <button type="button" className="boton boton-secundario" onClick={onCancelar}>
          Cancelar
        </button>
        <button type="submit" className="boton">
          Guardar
        </button>
      </div>
    </form>
  );
}

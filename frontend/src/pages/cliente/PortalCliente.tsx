import { useState, type ReactNode } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Alerta, Campo, Encabezado, EstadoCarga, Insignia, Modal, ModalMotivo, Tarjeta, Vacio } from '../../components/Comunes';
import { FormularioMascota, SelectorHorario } from '../../components/Formularios';
import { useAuth } from '../../contexts/AuthContext';
import { mensajeDeError, useCarga } from '../../hooks/useCarga';
import type { Cita, Pedido } from '../../interfaces/tipos';
import { citasService, mascotasService, pedidosService, serviciosService } from '../../services/servicios';
import { ahoraIsoLocal, edad, etiqueta, formatoFecha, formatoFechaHora, formatoMoneda, hoyIso } from '../../utils/formato';
import { ResumenPedido } from '../Tienda';

const ahoraLocal = () => ahoraIsoLocal();

/** Dashboard del cliente: resumen de mascotas, proximas citas y pedidos. */
export function PortalCliente() {
  const { usuario } = useAuth();
  const ubicacion = useLocation();
  const [bienvenida, setBienvenida] = useState(!!(ubicacion.state as { bienvenida?: boolean } | null)?.bienvenida);
  const carga = useCarga(
    () => Promise.all([mascotasService.listar(), citasService.listar(), pedidosService.listar()]),
    [],
  );
  const [mascotas, citas, pedidos] = carga.datos ?? [[], [], []];
  const proximas = citas.filter((c) => ['PENDIENTE', 'CONFIRMADA'].includes(c.estado) && c.inicio >= hoyIso()).reverse();

  return (
    <>
      <Encabezado titulo={`Hola, ${usuario?.nombre.split(' ')[0] ?? ''}`} migas={[{ texto: 'Inicio', a: '/' }]} />
      {bienvenida && (
        <Alerta tipo="exito" onCerrar={() => setBienvenida(false)}>
          Tu cuenta fue creada correctamente. Registra a tu mascota para solicitar citas.
        </Alerta>
      )}
      <EstadoCarga cargando={carga.cargando} error={carga.error} onReintentar={carga.recargar}>
        <div className="rejilla rejilla-3" style={{ marginBottom: 20 }}>
          <Link to="/portal/mascotas" className="metrica metrica-verde" style={{ textDecoration: 'none', color: 'inherit' }}>
            <span>Mis mascotas</span>
            <strong>{mascotas.length}</strong>
          </Link>
          <Link to="/portal/citas" className="metrica metrica-naranja" style={{ textDecoration: 'none', color: 'inherit' }}>
            <span>Próximas citas</span>
            <strong>{proximas.length}</strong>
          </Link>
          <Link to="/portal/pedidos" className="metrica metrica-morado" style={{ textDecoration: 'none', color: 'inherit' }}>
            <span>Pedidos en curso</span>
            <strong>{pedidos.filter((p) => ['PENDIENTE', 'CONFIRMADO', 'DESPACHADO'].includes(p.estado)).length}</strong>
          </Link>
        </div>
        <div className="rejilla rejilla-2">
          <Tarjeta titulo="Próximas citas" acciones={<Link className="boton boton-pequeno" to="/portal/citas/nueva">Solicitar cita</Link>}>
            {proximas.length ? (
              <ul className="lista-alertas">
                {proximas.slice(0, 5).map((c) => (
                  <li key={c.id} style={{ justifyContent: 'space-between' }}>
                    <span>
                      <strong>{formatoFechaHora(c.inicio)}</strong> · {c.servicioNombre} · {c.mascotaNombre}
                    </span>
                    <Insignia valor={c.estado} />
                  </li>
                ))}
              </ul>
            ) : (
              <Vacio>No tienes citas próximas.</Vacio>
            )}
          </Tarjeta>
          <Tarjeta titulo="Mis mascotas" acciones={<Link className="boton boton-pequeno" to="/portal/mascotas">Gestionar</Link>}>
            {mascotas.length ? (
              <ul className="lista-alertas">
                {mascotas.map((m) => (
                  <li key={m.id}>
                    <Link to={`/portal/mascotas/${m.id}`}>{m.nombre}</Link>&nbsp;· {etiqueta(m.especie)} · {m.raza}
                  </li>
                ))}
              </ul>
            ) : (
              <Vacio>Aún no registras mascotas.</Vacio>
            )}
          </Tarjeta>
        </div>
      </EstadoCarga>
    </>
  );
}

/** Listado y registro de las mascotas del cliente (HU-05, HU-21). */
export function MisMascotas() {
  const mascotas = useCarga(() => mascotasService.listar(), []);
  const [formulario, setFormulario] = useState(false);
  const [mensaje, setMensaje] = useState<string | null>(null);
  return (
    <>
      <Encabezado
        titulo="Mis mascotas"
        migas={[{ texto: 'Portal', a: '/portal' }]}
        acciones={
          <button className="boton" type="button" onClick={() => setFormulario(true)}>
            Registrar mascota
          </button>
        }
      />
      {mensaje && (
        <Alerta tipo="exito" onCerrar={() => setMensaje(null)}>
          {mensaje}
        </Alerta>
      )}
      <EstadoCarga cargando={mascotas.cargando} error={mascotas.error} onReintentar={mascotas.recargar}>
        {mascotas.datos?.length ? (
          <div className="rejilla rejilla-auto">
            {mascotas.datos.map((m) => (
              <article key={m.id} className="tarjeta-item">
                <h3>{m.nombre}</h3>
                <p>
                  {etiqueta(m.especie)} · {m.raza} · {etiqueta(m.sexo)}
                  <br />
                  {edad(m.fechaNacimiento)} · {m.peso} kg
                </p>
                <Link className="boton boton-secundario" to={`/portal/mascotas/${m.id}`}>
                  Ver ficha e historial
                </Link>
              </article>
            ))}
          </div>
        ) : (
          <Tarjeta>
            <Vacio>Aún no registras mascotas.</Vacio>
          </Tarjeta>
        )}
      </EstadoCarga>
      {formulario && (
        <Modal titulo="Registrar mascota" onCerrar={() => setFormulario(false)}>
          <FormularioMascota
            conPropietario={false}
            onCancelar={() => setFormulario(false)}
            onGuardar={async (d) => {
              const m = await mascotasService.crear(d);
              setFormulario(false);
              setMensaje(`${m.nombre} fue registrada correctamente.`);
              await mascotas.recargar();
            }}
          />
        </Modal>
      )}
    </>
  );
}

/** HU-09: solicitud de cita seleccionando mascota, servicio, fecha y hora disponible. */
export function SolicitarCita() {
  const navegar = useNavigate();
  const datos = useCarga(() => Promise.all([mascotasService.listar(), serviciosService.activos()]), []);
  const [mascotaId, setMascotaId] = useState('');
  const [servicioId, setServicioId] = useState('');
  const [fecha, setFecha] = useState(hoyIso(1));
  const [inicio, setInicio] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [resumen, setResumen] = useState<Cita | null>(null);
  const [mascotas, servicios] = datos.datos ?? [[], []];

  if (resumen) {
    return (
      <>
        <Encabezado titulo="Cita solicitada" migas={[{ texto: 'Portal', a: '/portal' }, { texto: 'Citas', a: '/portal/citas' }]} />
        <Tarjeta titulo="Resumen de la cita">
          <Alerta tipo="exito">Tu solicitud quedó registrada y está pendiente de confirmación por el personal.</Alerta>
          <p>
            <strong>Mascota:</strong> {resumen.mascotaNombre}
            <br />
            <strong>Servicio:</strong> {resumen.servicioNombre}
            <br />
            <strong>Fecha y hora:</strong> {formatoFechaHora(resumen.inicio)} a {resumen.fin.slice(11, 16)}
            <br />
            <strong>Estado:</strong> <Insignia valor={resumen.estado} />
          </p>
          <button className="boton" type="button" onClick={() => navegar('/portal/citas')}>
            Ver mis citas
          </button>
        </Tarjeta>
      </>
    );
  }

  return (
    <>
      <Encabezado titulo="Solicitar nueva cita" migas={[{ texto: 'Portal', a: '/portal' }, { texto: 'Citas', a: '/portal/citas' }]} />
      <EstadoCarga cargando={datos.cargando} error={datos.error} onReintentar={datos.recargar}>
        {!mascotas.length ? (
          <Alerta tipo="aviso">
            Primero <Link to="/portal/mascotas">registra una mascota</Link> para poder solicitar citas.
          </Alerta>
        ) : (
          <Tarjeta titulo="Solicitar nueva cita">
            {error && <Alerta tipo="error">{error}</Alerta>}
            <form
              className="formulario"
              onSubmit={async (e) => {
                e.preventDefault();
                setError(null);
                if (!mascotaId || !servicioId || !inicio) return setError('Selecciona la mascota, el servicio y una hora disponible');
                setEnviando(true);
                try {
                  setResumen(await citasService.solicitar(mascotaId, servicioId, inicio));
                } catch (err) {
                  setError(mensajeDeError(err));
                  setInicio('');
                } finally {
                  setEnviando(false);
                }
              }}
            >
              <Campo etiqueta="Mascota" requerido>
                <select value={mascotaId} onChange={(e) => setMascotaId(e.target.value)}>
                  <option value="">Seleccionar...</option>
                  {mascotas.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.nombre}
                    </option>
                  ))}
                </select>
              </Campo>
              <Campo etiqueta="Servicio" requerido>
                <select
                  value={servicioId}
                  onChange={(e) => {
                    setServicioId(e.target.value);
                    setInicio('');
                  }}
                >
                  <option value="">Seleccionar...</option>
                  {servicios.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.nombre} ({etiqueta(s.categoria)}) · {s.duracionMinutos} min · {formatoMoneda(s.precio)}
                    </option>
                  ))}
                </select>
              </Campo>
              <div className="completo">
                <Campo etiqueta="Fecha" requerido>
                  <input
                    type="date"
                    min={hoyIso()}
                    max={hoyIso(90)}
                    value={fecha}
                    onChange={(e) => {
                      setFecha(e.target.value);
                      setInicio('');
                    }}
                  />
                </Campo>
              </div>
              <div className="completo">
                <span className="campo-etiqueta">Horas disponibles</span>
                <SelectorHorario servicioId={servicioId} fecha={fecha} valor={inicio} onElegir={setInicio} />
              </div>
              <div className="acciones-formulario">
                <Link className="boton boton-secundario" to="/portal/citas">
                  Cancelar
                </Link>
                <button type="submit" className="boton" disabled={enviando || !inicio}>
                  {enviando ? 'Enviando...' : 'Confirmar'}
                </button>
              </div>
            </form>
          </Tarjeta>
        )}
      </EstadoCarga>
    </>
  );
}

/** Citas del cliente con cancelacion (HU-23) y reprogramacion (RA-07). */
export function MisCitas() {
  const citas = useCarga(() => citasService.listar(), []);
  const [cancelar, setCancelar] = useState<Cita | null>(null);
  const [reprogramar, setReprogramar] = useState<Cita | null>(null);
  const [mensaje, setMensaje] = useState<string | null>(null);
  return (
    <>
      <Encabezado titulo="Mis citas" migas={[{ texto: 'Portal', a: '/portal' }]} acciones={<Link className="boton" to="/portal/citas/nueva">Solicitar cita</Link>} />
      {mensaje && (
        <Alerta tipo="exito" onCerrar={() => setMensaje(null)}>
          {mensaje}
        </Alerta>
      )}
      <Tarjeta>
        <EstadoCarga cargando={citas.cargando} error={citas.error} onReintentar={citas.recargar}>
          <TablaCitas
            citas={citas.datos ?? []}
            acciones={(c) =>
              ['PENDIENTE', 'CONFIRMADA'].includes(c.estado) && c.inicio > ahoraLocal() ? (
                <>
                  <button className="boton boton-secundario boton-pequeno" type="button" onClick={() => setReprogramar(c)}>
                    Reprogramar
                  </button>
                  <button className="boton boton-peligro boton-pequeno" type="button" onClick={() => setCancelar(c)}>
                    Cancelar
                  </button>
                </>
              ) : null
            }
          />
        </EstadoCarga>
      </Tarjeta>
      {cancelar && (
        <ModalMotivo
          titulo={`Cancelar cita del ${formatoFechaHora(cancelar.inicio)}`}
          boton="Cancelar cita"
          onCerrar={() => setCancelar(null)}
          onConfirmar={async (motivo) => {
            await citasService.cancelar(cancelar.id, motivo);
            setCancelar(null);
            setMensaje('La cita fue cancelada y el horario quedó disponible.');
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
            setMensaje('La cita fue reprogramada.');
            await citas.recargar();
          }}
        />
      )}
    </>
  );
}

export function TablaCitas({ citas, acciones, mostrarCliente = false }: { citas: Cita[]; acciones?: (c: Cita) => ReactNode; mostrarCliente?: boolean }) {
  if (!citas.length) return <Vacio>No hay citas para mostrar.</Vacio>;
  return (
    <div className="tabla-contenedor">
      <table>
        <thead>
          <tr>
            <th>Fecha y hora</th>
            {mostrarCliente && <th>Cliente</th>}
            <th>Mascota</th>
            <th>Servicio</th>
            <th>Profesional</th>
            <th>Estado</th>
            {acciones && <th>Acciones</th>}
          </tr>
        </thead>
        <tbody>
          {citas.map((c) => (
            <tr key={c.id}>
              <td>{formatoFechaHora(c.inicio)}</td>
              {mostrarCliente && <td>{c.clienteNombre}</td>}
              <td>{c.mascotaNombre}</td>
              <td>
                {c.servicioNombre}
                <br />
                <small>{etiqueta(c.agenda)}</small>
              </td>
              <td>{c.veterinarioNombre ?? '—'}</td>
              <td>
                <Insignia valor={c.estado} />
                {c.motivo && (
                  <>
                    <br />
                    <small title={c.motivo}>Motivo: {c.motivo}</small>
                  </>
                )}
              </td>
              {acciones && <td className="celda-acciones">{acciones(c)}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function ModalReprogramar({ cita, onCerrar, onListo }: { cita: Cita; onCerrar: () => void; onListo: () => Promise<void> }) {
  const [fecha, setFecha] = useState(cita.inicio.slice(0, 10));
  const [inicio, setInicio] = useState('');
  const [error, setError] = useState<string | null>(null);
  return (
    <Modal titulo={`Reprogramar ${cita.servicioNombre} de ${cita.mascotaNombre}`} onCerrar={onCerrar}>
      {error && <Alerta tipo="error">{error}</Alerta>}
      <Campo etiqueta="Nueva fecha" requerido>
        <input
          type="date"
          min={hoyIso()}
          max={hoyIso(90)}
          value={fecha}
          onChange={(e) => {
            setFecha(e.target.value);
            setInicio('');
          }}
        />
      </Campo>
      <p className="campo-etiqueta">Horas disponibles</p>
      <SelectorHorario servicioId={cita.servicioId} fecha={fecha} valor={inicio} onElegir={setInicio} />
      <div className="acciones-formulario">
        <button className="boton boton-secundario" type="button" onClick={onCerrar}>
          Volver
        </button>
        <button
          className="boton"
          type="button"
          disabled={!inicio}
          onClick={async () => {
            try {
              await citasService.reprogramar(cita.id, inicio);
              await onListo();
            } catch (e) {
              setError(mensajeDeError(e));
            }
          }}
        >
          Reprogramar
        </button>
      </div>
    </Modal>
  );
}

/** Pedidos del cliente con su estado (RPE-04) y cancelacion antes del despacho (RPE-05). */
export function MisPedidos() {
  const pedidos = useCarga(() => pedidosService.listar(), []);
  const [detalle, setDetalle] = useState<Pedido | null>(null);
  const [cancelar, setCancelar] = useState<Pedido | null>(null);
  const [mensaje, setMensaje] = useState<string | null>(null);
  return (
    <>
      <Encabezado titulo="Mis pedidos" migas={[{ texto: 'Portal', a: '/portal' }]} acciones={<Link className="boton" to="/catalogo">Ir al catálogo</Link>} />
      {mensaje && (
        <Alerta tipo="exito" onCerrar={() => setMensaje(null)}>
          {mensaje}
        </Alerta>
      )}
      <Tarjeta>
        <EstadoCarga cargando={pedidos.cargando} error={pedidos.error} onReintentar={pedidos.recargar}>
          <TablaPedidos
            pedidos={pedidos.datos ?? []}
            acciones={(p) => (
              <>
                <button className="boton boton-secundario boton-pequeno" type="button" onClick={() => setDetalle(p)}>
                  Ver detalle
                </button>
                {['PENDIENTE', 'CONFIRMADO'].includes(p.estado) && (
                  <button className="boton boton-peligro boton-pequeno" type="button" onClick={() => setCancelar(p)}>
                    Cancelar
                  </button>
                )}
              </>
            )}
          />
        </EstadoCarga>
      </Tarjeta>
      {detalle && (
        <Modal titulo={`Pedido ${detalle.numero}`} onCerrar={() => setDetalle(null)}>
          <p>
            <Insignia valor={detalle.estado} texto={etiquetaEstadoPedido(detalle)} /> · {formatoFechaHora(detalle.fecha)}
          </p>
          {detalle.motivoCancelacion && <Alerta tipo="aviso">Motivo de cancelación: {detalle.motivoCancelacion}</Alerta>}
          <ResumenPedido pedido={detalle} />
        </Modal>
      )}
      {cancelar && (
        <ModalMotivo
          titulo={`Cancelar pedido ${cancelar.numero}`}
          boton="Cancelar pedido"
          onCerrar={() => setCancelar(null)}
          onConfirmar={async (motivo) => {
            await pedidosService.cancelar(cancelar.id, motivo);
            setCancelar(null);
            setMensaje('El pedido fue cancelado.');
            await pedidos.recargar();
          }}
        />
      )}
    </>
  );
}

export function etiquetaEstadoPedido(p: Pedido): string {
  return p.estado === 'DESPACHADO' && p.modalidad === 'RECOGIDA' ? 'Listo para recoger' : etiqueta(p.estado);
}

export function TablaPedidos({ pedidos, acciones, mostrarCliente = false }: { pedidos: Pedido[]; acciones: (p: Pedido) => ReactNode; mostrarCliente?: boolean }) {
  if (!pedidos.length) return <Vacio>No hay pedidos para mostrar.</Vacio>;
  return (
    <div className="tabla-contenedor">
      <table>
        <thead>
          <tr>
            <th>Número</th>
            <th>Fecha</th>
            {mostrarCliente && <th>Cliente</th>}
            <th>Entrega</th>
            <th>Total</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {pedidos.map((p) => (
            <tr key={p.id}>
              <td>{p.numero}</td>
              <td>{formatoFecha(p.fecha)}</td>
              {mostrarCliente && <td>{p.clienteNombre}</td>}
              <td>{etiqueta(p.modalidad)}</td>
              <td>{formatoMoneda(p.total)}</td>
              <td>
                <Insignia valor={p.estado} texto={etiquetaEstadoPedido(p)} />
              </td>
              <td className="celda-acciones">{acciones(p)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

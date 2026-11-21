import { useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Alerta, Campo, Encabezado, EstadoCarga, Insignia, Modal, Paginacion, Tarjeta, Vacio } from '../../components/Comunes';
import { FormularioMascota } from '../../components/Formularios';
import { useAuth } from '../../contexts/AuthContext';
import { mensajeDeError, useCarga } from '../../hooks/useCarga';
import type { Cliente, ClienteDatos, Rol } from '../../interfaces/tipos';
import { ApiError } from '../../services/api';
import { citasService, clientesService, mascotasService, panelService, usuariosService } from '../../services/servicios';
import { edad, etiqueta, formatoHora, formatoInstante, formatoMoneda, hoyIso } from '../../utils/formato';
import { etiquetaEstadoPedido } from '../cliente/PortalCliente';

/** HU-24: dashboard principal con metricas y resumenes. */
export function Dashboard() {
  const panel = useCarga(() => Promise.all([panelService.resumen(), citasService.listar({ desde: hoyIso(), hasta: hoyIso() })]), []);
  const [resumen, citasHoy] = panel.datos ?? [null, []];
  const totalHoy = resumen ? Object.values(resumen.citasHoy).reduce((a, b) => a + (b ?? 0), 0) : 0;
  return (
    <>
      <Encabezado titulo="Dashboard principal" migas={[{ texto: 'Inicio', a: '/' }]} />
      <EstadoCarga cargando={panel.cargando} error={panel.error} onReintentar={panel.recargar}>
        {resumen && (
          <>
            <div className="rejilla rejilla-4" style={{ marginBottom: 20 }}>
              <div className="metrica metrica-azul">
                <span>Clientes</span>
                <strong>{resumen.totalClientes}</strong>
              </div>
              <div className="metrica metrica-verde">
                <span>Mascotas</span>
                <strong>{resumen.totalMascotas}</strong>
              </div>
              <div className="metrica metrica-naranja">
                <span>Citas hoy</span>
                <strong>{totalHoy}</strong>
                <small>
                  {resumen.citasHoy.PENDIENTE ?? 0} pendientes · {resumen.citasHoy.CONFIRMADA ?? 0} confirmadas · {resumen.citasHoy.ATENDIDA ?? 0} completadas
                </small>
              </div>
              <div className="metrica metrica-morado">
                <span>Pedidos pendientes</span>
                <strong>{resumen.pedidosPendientes}</strong>
              </div>
            </div>
            {resumen.citasPendientes > 0 && (
              <Alerta tipo="aviso">
                Hay {resumen.citasPendientes} citas pendientes de confirmación. <Link to="/panel/citas">Revisar solicitudes</Link>
              </Alerta>
            )}
            <div className="rejilla rejilla-2">
              <Tarjeta titulo="Citas de hoy" acciones={<Link to="/panel/citas">Ver agenda</Link>}>
                {citasHoy.length ? (
                  <div className="tabla-contenedor">
                    <table>
                      <thead>
                        <tr>
                          <th>Hora</th>
                          <th>Cliente</th>
                          <th>Mascota</th>
                          <th>Estado</th>
                        </tr>
                      </thead>
                      <tbody>
                        {citasHoy.map((c) => (
                          <tr key={c.id}>
                            <td>{formatoHora(c.inicio)}</td>
                            <td>{c.clienteNombre}</td>
                            <td>{c.mascotaNombre}</td>
                            <td>
                              <Insignia valor={c.estado} />
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <Vacio>No hay citas programadas para hoy.</Vacio>
                )}
              </Tarjeta>
              <Tarjeta titulo={<span style={{ color: 'var(--rojo)' }}>Alertas de stock</span>} acciones={<Link to="/panel/inventario">Inventario</Link>}>
                {resumen.alertasStock.length ? (
                  <ul className="lista-alertas">
                    {resumen.alertasStock.map((p) => (
                      <li key={p.id}>
                        <span className="punto-alerta" aria-hidden="true" />
                        <span>
                          {p.nombre}
                          <small>
                            {p.stock} uds · mínimo {p.stockMinimo}
                          </small>
                        </span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <Vacio>Todos los productos tienen stock suficiente.</Vacio>
                )}
              </Tarjeta>
            </div>
            <Tarjeta titulo="Pedidos recientes" acciones={<Link to="/panel/pedidos">Ver pedidos</Link>}>
              {resumen.pedidosRecientes.length ? (
                <ul className="lista-alertas">
                  {resumen.pedidosRecientes.map((p) => (
                    <li key={p.id}>
                      #{p.numero} | {p.clienteNombre} | {formatoMoneda(p.total)} | {etiquetaEstadoPedido(p)}
                    </li>
                  ))}
                </ul>
              ) : (
                <Vacio>Aún no hay pedidos.</Vacio>
              )}
            </Tarjeta>
          </>
        )}
      </EstadoCarga>
    </>
  );
}

const CLIENTE_VACIO: ClienteDatos = { nombre: '', cedula: '', telefono: '', email: '', direccion: '' };

/** Formulario de cliente (HU-01, HU-02). La cedula no es editable. */
function FormularioCliente({ inicial, onGuardar, onCancelar }: { inicial?: Cliente; onGuardar: (d: ClienteDatos) => Promise<void>; onCancelar: () => void }) {
  const [d, setD] = useState<ClienteDatos>(inicial ? { nombre: inicial.nombre, cedula: inicial.cedula, telefono: inicial.telefono, email: inicial.email, direccion: inicial.direccion } : CLIENTE_VACIO);
  const [errores, setErrores] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const cambiar = (k: keyof ClienteDatos) => (e: { target: { value: string } }) => setD({ ...d, [k]: e.target.value });

  async function enviar(e: FormEvent) {
    e.preventDefault();
    const v: Record<string, string> = {};
    if (!d.nombre.trim()) v.nombre = 'El nombre es obligatorio';
    if (!/^\d{5,15}$/.test(d.cedula.trim())) v.cedula = 'La cédula debe tener entre 5 y 15 dígitos';
    if (!/^[0-9+ ]{7,20}$/.test(d.telefono.trim())) v.telefono = 'Teléfono no válido';
    if (!/^\S+@\S+\.\S+$/.test(d.email.trim())) v.email = 'Email con formato no válido';
    if (!d.direccion.trim()) v.direccion = 'La dirección es obligatoria';
    setErrores(v);
    if (Object.keys(v).length) return;
    setEnviando(true);
    setError(null);
    try {
      await onGuardar(d);
    } catch (err) {
      setError(mensajeDeError(err));
      if (err instanceof ApiError) setErrores(err.errores);
      setEnviando(false);
    }
  }

  return (
    <form className="formulario" onSubmit={enviar} noValidate>
      {error && (
        <div className="completo">
          <Alerta tipo="error">{error}</Alerta>
        </div>
      )}
      <div className="completo">
        <Campo etiqueta="Nombre completo" requerido error={errores.nombre}>
          <input value={d.nombre} maxLength={120} onChange={cambiar('nombre')} />
        </Campo>
      </div>
      <Campo etiqueta="Cédula" requerido error={errores.cedula} ayuda={inicial ? 'La cédula no se puede modificar' : 'Debe ser única'}>
        <input value={d.cedula} onChange={cambiar('cedula')} disabled={!!inicial} inputMode="numeric" maxLength={15} />
      </Campo>
      <Campo etiqueta="Teléfono" requerido error={errores.telefono}>
        <input value={d.telefono} onChange={cambiar('telefono')} maxLength={20} />
      </Campo>
      <Campo etiqueta="Email" requerido error={errores.email}>
        <input type="email" value={d.email} onChange={cambiar('email')} maxLength={120} />
      </Campo>
      <Campo etiqueta="Dirección" requerido error={errores.direccion}>
        <input value={d.direccion} onChange={cambiar('direccion')} maxLength={200} />
      </Campo>
      <div className="acciones-formulario">
        <button type="button" className="boton boton-secundario" onClick={onCancelar}>
          Cancelar
        </button>
        <button type="submit" className="boton" disabled={enviando}>
          {enviando ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </form>
  );
}

/** HU-03/HU-04: listado paginado, ordenable y con busqueda; HU-01 registro. */
export function Clientes() {
  const { tieneRol } = useAuth();
  const esAdmin = tieneRol('ADMINISTRADOR');
  const [texto, setTexto] = useState('');
  const [q, setQ] = useState('');
  const [pagina, setPagina] = useState(0);
  const [orden, setOrden] = useState('nombre');
  const [nuevo, setNuevo] = useState(false);
  const [editar, setEditar] = useState<Cliente | null>(null);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const clientes = useCarga(() => clientesService.buscar(q, pagina, 10, orden), [q, pagina, orden]);

  useEffect(() => {
    const t = window.setTimeout(() => {
      setQ(texto);
      setPagina(0);
    }, 350);
    return () => window.clearTimeout(t);
  }, [texto]);

  async function desactivar(c: Cliente) {
    if (!window.confirm(`¿Desactivar a ${c.nombre}? También se desactivará su cuenta de acceso.`)) return;
    try {
      await clientesService.desactivar(c.id);
      setMensaje(`${c.nombre} fue desactivado.`);
      await clientes.recargar();
    } catch (e) {
      setError(mensajeDeError(e));
    }
  }

  return (
    <>
      <Encabezado
        titulo="Clientes"
        migas={[{ texto: 'Panel', a: '/panel' }]}
        acciones={esAdmin && <button className="boton" type="button" onClick={() => setNuevo(true)}>Registrar cliente</button>}
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
      <Tarjeta>
        <div className="filtros">
          <Campo etiqueta="Buscar" ayuda="Nombre (parcial), cédula exacta o teléfono">
            <input type="search" value={texto} onChange={(e) => setTexto(e.target.value)} />
          </Campo>
          <Campo etiqueta="Ordenar por">
            <select value={orden} onChange={(e) => setOrden(e.target.value)}>
              <option value="nombre">Nombre</option>
              <option value="fechaRegistro">Fecha de registro (recientes)</option>
            </select>
          </Campo>
        </div>
        <EstadoCarga cargando={clientes.cargando} error={clientes.error} onReintentar={clientes.recargar}>
          {clientes.datos?.contenido.length ? (
            <>
              <div className="tabla-contenedor">
                <table>
                  <thead>
                    <tr>
                      <th>Nombre</th>
                      <th>Cédula</th>
                      <th>Teléfono</th>
                      <th>Email</th>
                      <th>Registro</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {clientes.datos.contenido.map((c) => (
                      <tr key={c.id}>
                        <td>
                          <Link to={`/panel/clientes/${c.id}`}>{c.nombre}</Link>
                        </td>
                        <td>{c.cedula}</td>
                        <td>{c.telefono}</td>
                        <td>{c.email}</td>
                        <td>{formatoInstante(c.fechaRegistro).split(',')[0]}</td>
                        <td className="celda-acciones">
                          <Link className="boton boton-secundario boton-pequeno" to={`/panel/clientes/${c.id}`}>
                            Ver
                          </Link>
                          {esAdmin && (
                            <>
                              <button className="boton boton-secundario boton-pequeno" type="button" onClick={() => setEditar(c)}>
                                Editar
                              </button>
                              <button className="boton boton-peligro boton-pequeno" type="button" onClick={() => desactivar(c)}>
                                Eliminar
                              </button>
                            </>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Paginacion pagina={clientes.datos.pagina} totalPaginas={clientes.datos.totalPaginas} onCambiar={setPagina} />
              <p className="ayuda">{clientes.datos.totalElementos} clientes activos</p>
            </>
          ) : (
            <Vacio>No se encontraron clientes.</Vacio>
          )}
        </EstadoCarga>
      </Tarjeta>
      {nuevo && (
        <Modal titulo="Registrar cliente" onCerrar={() => setNuevo(false)}>
          <FormularioCliente
            onCancelar={() => setNuevo(false)}
            onGuardar={async (d) => {
              const c = await clientesService.crear(d);
              setNuevo(false);
              setMensaje(`Cliente ${c.nombre} registrado correctamente.`);
              await clientes.recargar();
            }}
          />
        </Modal>
      )}
      {editar && (
        <Modal titulo={`Editar ${editar.nombre}`} onCerrar={() => setEditar(null)}>
          <FormularioCliente
            inicial={editar}
            onCancelar={() => setEditar(null)}
            onGuardar={async (d) => {
              await clientesService.actualizar(editar.id, d);
              setEditar(null);
              setMensaje('Datos del cliente actualizados.');
              await clientes.recargar();
            }}
          />
        </Modal>
      )}
    </>
  );
}

/** Ficha del cliente: datos, mascotas (HU-05 criterio 5) e historial de cambios (HU-02). */
export function ClienteDetallePagina() {
  const { id = '' } = useParams();
  const { tieneRol } = useAuth();
  const esAdmin = tieneRol('ADMINISTRADOR');
  const detalle = useCarga(() => clientesService.detalle(id), [id]);
  const [editar, setEditar] = useState(false);
  const [nuevaMascota, setNuevaMascota] = useState(false);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const c = detalle.datos?.cliente;
  return (
    <EstadoCarga cargando={detalle.cargando} error={detalle.error} onReintentar={detalle.recargar}>
      {c && detalle.datos && (
        <>
          <Encabezado
            titulo={c.nombre}
            migas={[{ texto: 'Panel', a: '/panel' }, { texto: 'Clientes', a: '/panel/clientes' }]}
            acciones={
              <>
                {esAdmin && (
                  <button className="boton boton-secundario" type="button" onClick={() => setEditar(true)}>
                    Editar
                  </button>
                )}
                <button className="boton" type="button" onClick={() => setNuevaMascota(true)}>
                  Registrar mascota
                </button>
              </>
            }
          />
          {mensaje && (
            <Alerta tipo="exito" onCerrar={() => setMensaje(null)}>
              {mensaje}
            </Alerta>
          )}
          <div className="rejilla rejilla-2">
            <Tarjeta titulo="Datos personales">
              <p>
                <strong>Cédula:</strong> {c.cedula}
                <br />
                <strong>Teléfono:</strong> {c.telefono}
                <br />
                <strong>Email:</strong> {c.email}
                <br />
                <strong>Dirección:</strong> {c.direccion}
                <br />
                <strong>Registrado:</strong> {formatoInstante(c.fechaRegistro)}
              </p>
            </Tarjeta>
            <Tarjeta titulo="Mascotas">
              {detalle.datos.mascotas.length ? (
                <ul className="lista-alertas">
                  {detalle.datos.mascotas.map((m) => (
                    <li key={m.id}>
                      <Link to={`/panel/mascotas/${m.id}`}>{m.nombre}</Link>&nbsp;· {etiqueta(m.especie)} · {m.raza} · {edad(m.fechaNacimiento)}
                    </li>
                  ))}
                </ul>
              ) : (
                <Vacio>Este cliente no tiene mascotas registradas.</Vacio>
              )}
            </Tarjeta>
          </div>
          <Tarjeta titulo="Historial de cambios">
            {detalle.datos.historial.length ? (
              <div className="tabla-contenedor">
                <table>
                  <thead>
                    <tr>
                      <th>Fecha</th>
                      <th>Usuario</th>
                      <th>Cambio</th>
                    </tr>
                  </thead>
                  <tbody>
                    {detalle.datos.historial.map((h, i) => (
                      <tr key={i}>
                        <td>{formatoInstante(h.fecha)}</td>
                        <td>{h.usuario}</td>
                        <td>{h.detalle}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <Vacio>Sin cambios registrados.</Vacio>
            )}
          </Tarjeta>
          {editar && (
            <Modal titulo={`Editar ${c.nombre}`} onCerrar={() => setEditar(false)}>
              <FormularioCliente
                inicial={c}
                onCancelar={() => setEditar(false)}
                onGuardar={async (d) => {
                  await clientesService.actualizar(c.id, d);
                  setEditar(false);
                  setMensaje('Datos del cliente actualizados.');
                  await detalle.recargar();
                }}
              />
            </Modal>
          )}
          {nuevaMascota && (
            <Modal titulo={`Registrar mascota de ${c.nombre}`} onCerrar={() => setNuevaMascota(false)}>
              <FormularioMascota
                conPropietario
                propietarioFijo={c}
                onCancelar={() => setNuevaMascota(false)}
                onGuardar={async (d) => {
                  const m = await mascotasService.crear(d);
                  setNuevaMascota(false);
                  setMensaje(`${m.nombre} fue registrada correctamente.`);
                  await detalle.recargar();
                }}
              />
            </Modal>
          )}
        </>
      )}
    </EstadoCarga>
  );
}

/** Listado de mascotas para el personal y registro con buscador de propietario (HU-05). */
export function MascotasAdmin() {
  const navegar = useNavigate();
  const mascotas = useCarga(() => mascotasService.listar(), []);
  const [filtro, setFiltro] = useState('');
  const [nueva, setNueva] = useState(false);
  const lista = (mascotas.datos ?? []).filter((m) =>
    `${m.nombre} ${m.propietarioNombre ?? ''} ${m.raza}`.toLowerCase().includes(filtro.toLowerCase()),
  );
  return (
    <>
      <Encabezado titulo="Mascotas" migas={[{ texto: 'Panel', a: '/panel' }]} acciones={<button className="boton" type="button" onClick={() => setNueva(true)}>Registrar mascota</button>} />
      <Tarjeta>
        <div className="filtros">
          <Campo etiqueta="Filtrar" ayuda="Nombre de la mascota, raza o propietario">
            <input type="search" value={filtro} onChange={(e) => setFiltro(e.target.value)} />
          </Campo>
        </div>
        <EstadoCarga cargando={mascotas.cargando} error={mascotas.error} onReintentar={mascotas.recargar}>
          {lista.length ? (
            <div className="tabla-contenedor">
              <table>
                <thead>
                  <tr>
                    <th>Nombre</th>
                    <th>Especie</th>
                    <th>Raza</th>
                    <th>Edad</th>
                    <th>Propietario</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {lista.map((m) => (
                    <tr key={m.id}>
                      <td>{m.nombre}</td>
                      <td>{etiqueta(m.especie)}</td>
                      <td>{m.raza}</td>
                      <td>{edad(m.fechaNacimiento)}</td>
                      <td>
                        <Link to={`/panel/clientes/${m.propietarioId}`}>{m.propietarioNombre}</Link>
                      </td>
                      <td>
                        <Link className="boton boton-secundario boton-pequeno" to={`/panel/mascotas/${m.id}`}>
                          Ficha e historial
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <Vacio>No hay mascotas registradas.</Vacio>
          )}
        </EstadoCarga>
      </Tarjeta>
      {nueva && (
        <Modal titulo="Registrar mascota" onCerrar={() => setNueva(false)}>
          <FormularioMascota
            conPropietario
            onCancelar={() => setNueva(false)}
            onGuardar={async (d) => {
              const m = await mascotasService.crear(d);
              navegar(`/panel/mascotas/${m.id}`);
            }}
          />
        </Modal>
      )}
    </>
  );
}

const ROLES: Rol[] = ['CLIENTE', 'VETERINARIO', 'ADMINISTRADOR'];

/** HU-20: listado de usuarios con sus roles, cambio de rol y desactivacion. */
export function Usuarios() {
  const { usuario: yo } = useAuth();
  const [rol, setRol] = useState<Rol | ''>('');
  const usuarios = useCarga(() => usuariosService.listar(rol), [rol]);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [nuevo, setNuevo] = useState(false);

  async function accion(f: () => Promise<unknown>, texto: string) {
    setError(null);
    try {
      await f();
      setMensaje(texto);
      await usuarios.recargar();
    } catch (e) {
      setError(mensajeDeError(e));
    }
  }

  return (
    <>
      <Encabezado titulo="Usuarios y roles" migas={[{ texto: 'Panel', a: '/panel' }]} acciones={<button className="boton" type="button" onClick={() => setNuevo(true)}>Crear cuenta de personal</button>} />
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
        <div className="filtros">
          <Campo etiqueta="Rol">
            <select value={rol} onChange={(e) => setRol(e.target.value as Rol | '')}>
              <option value="">Todos</option>
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {etiqueta(r)}
                </option>
              ))}
            </select>
          </Campo>
        </div>
        <EstadoCarga cargando={usuarios.cargando} error={usuarios.error} onReintentar={usuarios.recargar}>
          {usuarios.datos?.length ? (
            <div className="tabla-contenedor">
              <table>
                <thead>
                  <tr>
                    <th>Nombre</th>
                    <th>Email</th>
                    <th>Rol</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {usuarios.datos.map((u) => (
                    <tr key={u.id} className={u.activo ? '' : 'fila-inactiva'}>
                      <td>{u.nombre}</td>
                      <td>{u.email}</td>
                      <td>
                        <select
                          aria-label={`Rol de ${u.nombre}`}
                          value={u.rol}
                          disabled={u.id === yo?.id}
                          onChange={(e) => accion(() => usuariosService.cambiarRol(u.id, e.target.value as Rol), `Rol de ${u.nombre} actualizado.`)}
                        >
                          {ROLES.map((r) => (
                            <option key={r} value={r} disabled={r === 'CLIENTE' && !u.clienteId}>
                              {etiqueta(r)}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td>{u.activo ? <Insignia valor="ENTRADA" texto="Activo" /> : <Insignia valor="CANCELADO" texto="Inactivo" />}</td>
                      <td>
                        {u.id !== yo?.id && (
                          <button
                            type="button"
                            className={`boton boton-pequeno ${u.activo ? 'boton-peligro' : 'boton-exito'}`}
                            onClick={() => accion(() => usuariosService.cambiarEstado(u.id, !u.activo), `Cuenta de ${u.nombre} ${u.activo ? 'desactivada' : 'activada'}.`)}
                          >
                            {u.activo ? 'Desactivar' : 'Activar'}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <Vacio>No hay usuarios con ese rol.</Vacio>
          )}
        </EstadoCarga>
      </Tarjeta>
      {nuevo && (
        <Modal titulo="Crear cuenta de personal" onCerrar={() => setNuevo(false)}>
          <FormularioPersonal
            onCancelar={() => setNuevo(false)}
            onGuardar={async (d) => {
              await usuariosService.crearPersonal(d);
              setNuevo(false);
              setMensaje(`Cuenta de ${d.nombre} creada.`);
              await usuarios.recargar();
            }}
          />
        </Modal>
      )}
    </>
  );
}

function FormularioPersonal({ onGuardar, onCancelar }: { onGuardar: (d: { nombre: string; email: string; clave: string; rol: Rol }) => Promise<void>; onCancelar: () => void }) {
  const [d, setD] = useState({ nombre: '', email: '', clave: '', rol: 'VETERINARIO' as Rol });
  const [error, setError] = useState<string | null>(null);
  return (
    <form
      className="formulario"
      onSubmit={async (e) => {
        e.preventDefault();
        if (!d.nombre.trim() || !/^\S+@\S+\.\S+$/.test(d.email)) return setError('Completa el nombre y un email válido');
        if (d.clave.length < 8) return setError('La contraseña debe tener mínimo 8 caracteres');
        try {
          await onGuardar(d);
        } catch (err) {
          setError(mensajeDeError(err));
        }
      }}
    >
      {error && (
        <div className="completo">
          <Alerta tipo="error">{error}</Alerta>
        </div>
      )}
      <Campo etiqueta="Nombre" requerido>
        <input value={d.nombre} onChange={(e) => setD({ ...d, nombre: e.target.value })} />
      </Campo>
      <Campo etiqueta="Email" requerido>
        <input type="email" value={d.email} onChange={(e) => setD({ ...d, email: e.target.value })} />
      </Campo>
      <Campo etiqueta="Contraseña inicial" requerido ayuda="Mínimo 8 caracteres">
        <input type="password" value={d.clave} onChange={(e) => setD({ ...d, clave: e.target.value })} autoComplete="new-password" />
      </Campo>
      <Campo etiqueta="Rol" requerido>
        <select value={d.rol} onChange={(e) => setD({ ...d, rol: e.target.value as Rol })}>
          <option value="VETERINARIO">{etiqueta('VETERINARIO')}</option>
          <option value="ADMINISTRADOR">{etiqueta('ADMINISTRADOR')}</option>
        </select>
      </Campo>
      <div className="acciones-formulario">
        <button type="button" className="boton boton-secundario" onClick={onCancelar}>
          Cancelar
        </button>
        <button type="submit" className="boton">
          Crear cuenta
        </button>
      </div>
    </form>
  );
}


import { useState, type FormEvent } from 'react';
import { Alerta, Campo, Encabezado, EstadoCarga, Insignia, Modal, ModalMotivo, Tarjeta, Vacio } from '../../components/Comunes';
import { useAuth } from '../../contexts/AuthContext';
import { mensajeDeError, useCarga } from '../../hooks/useCarga';
import type { CategoriaProducto, CategoriaServicio, EstadoPedido, Pedido, Producto, ProductoDatos, Servicio, ServicioDatos, TipoMovimiento } from '../../interfaces/tipos';
import { inventarioService, pedidosService, productosService, serviciosService } from '../../services/servicios';
import { etiqueta, formatoFechaHora, formatoMoneda } from '../../utils/formato';
import { TablaPedidos, etiquetaEstadoPedido } from '../cliente/PortalCliente';
import { ResumenPedido } from '../Tienda';

const CATEGORIAS_PRODUCTO: CategoriaProducto[] = ['MEDICAMENTOS', 'ALIMENTOS', 'ACCESORIOS', 'HIGIENE', 'JUGUETES'];

function useMensajes() {
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const vista = (
    <>
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
    </>
  );
  return { setMensaje, setError, vista };
}

/** HU-12: listado administrable de servicios con activacion y desactivacion. */
export function ServiciosAdmin() {
  const servicios = useCarga(() => serviciosService.todos(), []);
  const [editar, setEditar] = useState<Servicio | 'nuevo' | null>(null);
  const m = useMensajes();
  return (
    <>
      <Encabezado titulo="Servicios" migas={[{ texto: 'Panel', a: '/panel' }]} acciones={<button className="boton" type="button" onClick={() => setEditar('nuevo')}>Registrar servicio</button>} />
      {m.vista}
      <Tarjeta>
        <EstadoCarga cargando={servicios.cargando} error={servicios.error} onReintentar={servicios.recargar}>
          {servicios.datos?.length ? (
            <div className="tabla-contenedor">
              <table>
                <thead>
                  <tr>
                    <th>Nombre</th>
                    <th>Categoría</th>
                    <th>Precio</th>
                    <th>Duración</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {servicios.datos.map((s) => (
                    <tr key={s.id} className={s.activo ? '' : 'fila-inactiva'}>
                      <td>
                        {s.nombre}
                        <br />
                        <small>{s.descripcion}</small>
                      </td>
                      <td>{etiqueta(s.categoria)}</td>
                      <td>{formatoMoneda(s.precio)}</td>
                      <td>{s.duracionMinutos} min</td>
                      <td>{s.activo ? <Insignia valor="ENTRADA" texto="Activo" /> : <Insignia valor="CANCELADO" texto="Inactivo" />}</td>
                      <td className="celda-acciones">
                        <button className="boton boton-secundario boton-pequeno" type="button" onClick={() => setEditar(s)}>
                          Editar
                        </button>
                        <button
                          className={`boton boton-pequeno ${s.activo ? 'boton-peligro' : 'boton-exito'}`}
                          type="button"
                          onClick={async () => {
                            try {
                              await serviciosService.cambiarEstado(s.id, !s.activo);
                              m.setMensaje(`Servicio ${s.activo ? 'desactivado' : 'activado'}.`);
                              await servicios.recargar();
                            } catch (e) {
                              m.setError(mensajeDeError(e));
                            }
                          }}
                        >
                          {s.activo ? 'Desactivar' : 'Activar'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <Vacio>No hay servicios registrados.</Vacio>
          )}
        </EstadoCarga>
      </Tarjeta>
      {editar && (
        <Modal titulo={editar === 'nuevo' ? 'Registrar servicio' : `Editar ${editar.nombre}`} onCerrar={() => setEditar(null)}>
          <FormularioServicio
            inicial={editar === 'nuevo' ? undefined : editar}
            onCancelar={() => setEditar(null)}
            onGuardar={async (d) => {
              if (editar === 'nuevo') await serviciosService.crear(d);
              else await serviciosService.actualizar(editar.id, d);
              setEditar(null);
              m.setMensaje('Servicio guardado correctamente.');
              await servicios.recargar();
            }}
          />
        </Modal>
      )}
    </>
  );
}

function FormularioServicio({ inicial, onGuardar, onCancelar }: { inicial?: Servicio; onGuardar: (d: ServicioDatos) => Promise<void>; onCancelar: () => void }) {
  const [d, setD] = useState<ServicioDatos>({
    nombre: inicial?.nombre ?? '',
    descripcion: inicial?.descripcion ?? '',
    categoria: inicial?.categoria ?? 'VETERINARIA',
    precio: inicial?.precio ?? 0,
    duracionMinutos: inicial?.duracionMinutos ?? 30,
    activo: inicial?.activo ?? true,
  });
  const [error, setError] = useState<string | null>(null);
  async function enviar(e: FormEvent) {
    e.preventDefault();
    if (!d.nombre.trim() || !d.descripcion.trim()) return setError('Nombre y descripción son obligatorios');
    if (d.precio < 0) return setError('El precio no puede ser negativo');
    if (d.duracionMinutos < 15 || d.duracionMinutos > 240) return setError('La duración debe estar entre 15 y 240 minutos');
    try {
      await onGuardar(d);
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
      <Campo etiqueta="Nombre del servicio" requerido>
        <input value={d.nombre} maxLength={100} onChange={(e) => setD({ ...d, nombre: e.target.value })} />
      </Campo>
      <Campo etiqueta="Categoría (agenda)" requerido>
        <select value={d.categoria} onChange={(e) => setD({ ...d, categoria: e.target.value as CategoriaServicio })}>
          <option value="VETERINARIA">Veterinaria</option>
          <option value="PELUQUERIA">Peluquería</option>
        </select>
      </Campo>
      <div className="completo">
        <Campo etiqueta="Descripción" requerido>
          <textarea rows={2} maxLength={500} value={d.descripcion} onChange={(e) => setD({ ...d, descripcion: e.target.value })} />
        </Campo>
      </div>
      <Campo etiqueta="Precio (COP)" requerido>
        <input type="number" min={0} step={100} value={d.precio} onChange={(e) => setD({ ...d, precio: Number(e.target.value) })} />
      </Campo>
      <Campo etiqueta="Duración estimada (minutos)" requerido>
        <input type="number" min={15} max={240} step={15} value={d.duracionMinutos} onChange={(e) => setD({ ...d, duracionMinutos: Number(e.target.value) })} />
      </Campo>
      <label className="casilla completo">
        <input type="checkbox" checked={d.activo} onChange={(e) => setD({ ...d, activo: e.target.checked })} /> Visible para los clientes
      </label>
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

/** HU-13 / RP-01 a RP-05: catalogo administrable de productos y medicamentos. */
export function ProductosAdmin() {
  const [q, setQ] = useState('');
  const [categoria, setCategoria] = useState<CategoriaProducto | ''>('');
  const productos = useCarga(() => productosService.gestion(q, categoria), [q, categoria]);
  const [editar, setEditar] = useState<Producto | 'nuevo' | null>(null);
  const m = useMensajes();
  return (
    <>
      <Encabezado titulo="Productos" migas={[{ texto: 'Panel', a: '/panel' }]} acciones={<button className="boton" type="button" onClick={() => setEditar('nuevo')}>Registrar producto</button>} />
      {m.vista}
      <Tarjeta>
        <div className="filtros">
          <Campo etiqueta="Buscar">
            <input type="search" value={q} onChange={(e) => setQ(e.target.value)} />
          </Campo>
          <Campo etiqueta="Categoría">
            <select value={categoria} onChange={(e) => setCategoria(e.target.value as CategoriaProducto | '')}>
              <option value="">Todas</option>
              {CATEGORIAS_PRODUCTO.map((c) => (
                <option key={c} value={c}>
                  {etiqueta(c)}
                </option>
              ))}
            </select>
          </Campo>
        </div>
        <EstadoCarga cargando={productos.cargando} error={productos.error} onReintentar={productos.recargar}>
          {productos.datos?.length ? (
            <div className="tabla-contenedor">
              <table>
                <thead>
                  <tr>
                    <th>Producto</th>
                    <th>Categoría</th>
                    <th>Precio</th>
                    <th>Stock</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {productos.datos.map((p) => (
                    <tr key={p.id} className={p.activo ? '' : 'fila-inactiva'}>
                      <td>
                        {p.nombre}
                        {p.requiereFormula && (
                          <>
                            <br />
                            <small>Requiere fórmula</small>
                          </>
                        )}
                      </td>
                      <td>{etiqueta(p.categoria)}</td>
                      <td>{formatoMoneda(p.precio)}</td>
                      <td>
                        {p.stock} {p.stockBajo && <Insignia valor="CANCELADO" texto={`Bajo (mín. ${p.stockMinimo})`} />}
                      </td>
                      <td>{p.activo ? <Insignia valor="ENTRADA" texto="Activo" /> : <Insignia valor="CANCELADO" texto="Inactivo" />}</td>
                      <td className="celda-acciones">
                        <button className="boton boton-secundario boton-pequeno" type="button" onClick={() => setEditar(p)}>
                          Editar
                        </button>
                        <button
                          className={`boton boton-pequeno ${p.activo ? 'boton-peligro' : 'boton-exito'}`}
                          type="button"
                          onClick={async () => {
                            try {
                              await productosService.cambiarEstado(p.id, !p.activo);
                              m.setMensaje(`Producto ${p.activo ? 'desactivado' : 'activado'}.`);
                              await productos.recargar();
                            } catch (e) {
                              m.setError(mensajeDeError(e));
                            }
                          }}
                        >
                          {p.activo ? 'Desactivar' : 'Activar'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <Vacio>No hay productos.</Vacio>
          )}
        </EstadoCarga>
      </Tarjeta>
      {editar && (
        <Modal titulo={editar === 'nuevo' ? 'Registrar producto' : `Editar ${editar.nombre}`} onCerrar={() => setEditar(null)}>
          <FormularioProducto
            inicial={editar === 'nuevo' ? undefined : editar}
            onCancelar={() => setEditar(null)}
            onGuardar={async (d) => {
              if (editar === 'nuevo') await productosService.crear(d);
              else await productosService.actualizar(editar.id, d);
              setEditar(null);
              m.setMensaje('Producto guardado correctamente.');
              await productos.recargar();
            }}
          />
        </Modal>
      )}
    </>
  );
}

function FormularioProducto({ inicial, onGuardar, onCancelar }: { inicial?: Producto; onGuardar: (d: ProductoDatos) => Promise<void>; onCancelar: () => void }) {
  const [d, setD] = useState<ProductoDatos>({
    nombre: inicial?.nombre ?? '',
    descripcion: inicial?.descripcion ?? '',
    categoria: inicial?.categoria ?? 'ALIMENTOS',
    precio: inicial?.precio ?? 0,
    stockInicial: inicial ? undefined : 0,
    stockMinimo: inicial?.stockMinimo ?? 5,
    requiereFormula: inicial?.requiereFormula ?? false,
  });
  const [error, setError] = useState<string | null>(null);
  async function enviar(e: FormEvent) {
    e.preventDefault();
    if (!d.nombre.trim() || !d.descripcion.trim()) return setError('Nombre y descripción son obligatorios');
    if (!(d.precio > 0)) return setError('El precio debe ser mayor a cero');
    if ((d.stockInicial ?? 0) < 0 || d.stockMinimo < 0) return setError('El stock no puede ser negativo');
    try {
      await onGuardar(d);
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
      <Campo etiqueta="Nombre del producto" requerido>
        <input value={d.nombre} maxLength={120} onChange={(e) => setD({ ...d, nombre: e.target.value })} />
      </Campo>
      <Campo etiqueta="Categoría" requerido>
        <select value={d.categoria} onChange={(e) => setD({ ...d, categoria: e.target.value as CategoriaProducto })}>
          {CATEGORIAS_PRODUCTO.map((c) => (
            <option key={c} value={c}>
              {etiqueta(c)}
            </option>
          ))}
        </select>
      </Campo>
      <div className="completo">
        <Campo etiqueta="Descripción" requerido>
          <textarea rows={2} maxLength={500} value={d.descripcion} onChange={(e) => setD({ ...d, descripcion: e.target.value })} />
        </Campo>
      </div>
      <Campo etiqueta="Precio unitario (COP)" requerido>
        <input type="number" min={1} step={100} value={d.precio} onChange={(e) => setD({ ...d, precio: Number(e.target.value) })} />
      </Campo>
      {inicial ? (
        <Campo etiqueta="Stock actual" ayuda="Se modifica con movimientos de inventario">
          <input value={inicial.stock} disabled />
        </Campo>
      ) : (
        <Campo etiqueta="Stock inicial" requerido>
          <input type="number" min={0} value={d.stockInicial ?? 0} onChange={(e) => setD({ ...d, stockInicial: Number(e.target.value) })} />
        </Campo>
      )}
      <Campo etiqueta="Stock mínimo (alerta)" requerido>
        <input type="number" min={0} value={d.stockMinimo} onChange={(e) => setD({ ...d, stockMinimo: Number(e.target.value) })} />
      </Campo>
      <label className="casilla">
        <input type="checkbox" checked={d.requiereFormula} onChange={(e) => setD({ ...d, requiereFormula: e.target.checked })} /> Requiere fórmula veterinaria (no se vende en línea)
      </label>
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

/** HU-14 y HU-15: movimientos de stock, existencias y alertas. */
export function Inventario() {
  const { tieneRol } = useAuth();
  const productos = useCarga(() => (tieneRol('ADMINISTRADOR') ? productosService.gestion() : productosService.catalogo()), []);
  const alertas = useCarga(() => inventarioService.alertas(), []);
  const [productoFiltro, setProductoFiltro] = useState('');
  const movimientos = useCarga(() => inventarioService.movimientos(productoFiltro || undefined), [productoFiltro]);
  const [d, setD] = useState({ productoId: '', tipo: 'ENTRADA' as TipoMovimiento, cantidad: 1, motivo: '' });
  const m = useMensajes();

  async function registrar(e: FormEvent) {
    e.preventDefault();
    m.setError(null);
    if (!d.productoId || !d.motivo.trim() || d.cantidad < 1) return m.setError('Selecciona el producto, una cantidad mayor a cero y el motivo');
    try {
      const mov = await inventarioService.registrar(d);
      m.setMensaje(`${etiqueta(mov.tipo)} registrada. Nuevo saldo de ${mov.productoNombre}: ${mov.saldo}`);
      setD({ ...d, cantidad: 1, motivo: '' });
      await Promise.all([productos.recargar(), alertas.recargar(), movimientos.recargar()]);
    } catch (err) {
      m.setError(mensajeDeError(err));
    }
  }

  const seleccionado = productos.datos?.find((p) => p.id === d.productoId);
  return (
    <>
      <Encabezado titulo="Inventario" migas={[{ texto: 'Panel', a: '/panel' }]} />
      {m.vista}
      <div className="rejilla rejilla-2">
        <Tarjeta titulo="Registrar entrada o salida">
          <form className="formulario" onSubmit={registrar} noValidate>
            <div className="completo">
              <Campo etiqueta="Producto" requerido ayuda={seleccionado ? `Stock actual: ${seleccionado.stock}` : undefined}>
                <select value={d.productoId} onChange={(e) => setD({ ...d, productoId: e.target.value })}>
                  <option value="">Seleccionar...</option>
                  {productos.datos?.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nombre} (stock {p.stock})
                    </option>
                  ))}
                </select>
              </Campo>
            </div>
            <Campo etiqueta="Tipo" requerido>
              <select value={d.tipo} onChange={(e) => setD({ ...d, tipo: e.target.value as TipoMovimiento })}>
                <option value="ENTRADA">Entrada (compra / proveedor)</option>
                <option value="SALIDA">Salida (venta / uso)</option>
              </select>
            </Campo>
            <Campo etiqueta="Cantidad" requerido>
              <input type="number" min={1} value={d.cantidad} onChange={(e) => setD({ ...d, cantidad: Number(e.target.value) })} />
            </Campo>
            <div className="completo">
              <Campo etiqueta="Motivo" requerido>
                <input value={d.motivo} maxLength={300} onChange={(e) => setD({ ...d, motivo: e.target.value })} placeholder="Ej. Compra a proveedor" />
              </Campo>
            </div>
            <div className="acciones-formulario">
              <button className="boton" type="submit">
                Registrar movimiento
              </button>
            </div>
          </form>
        </Tarjeta>
        <Tarjeta titulo={<span style={{ color: 'var(--rojo)' }}>Productos con stock bajo</span>}>
          <EstadoCarga cargando={alertas.cargando} error={alertas.error} onReintentar={alertas.recargar}>
            {alertas.datos?.length ? (
              <ul className="lista-alertas">
                {alertas.datos.map((p) => (
                  <li key={p.id}>
                    <span className="punto-alerta" aria-hidden="true" />
                    <span>
                      {p.nombre}
                      <small>
                        {p.stock} uds · mínimo configurado {p.stockMinimo}
                      </small>
                    </span>
                  </li>
                ))}
              </ul>
            ) : (
              <Vacio>No hay alertas de stock.</Vacio>
            )}
          </EstadoCarga>
        </Tarjeta>
      </div>
      <Tarjeta
        titulo="Historial de movimientos"
        acciones={
          <select aria-label="Filtrar por producto" value={productoFiltro} onChange={(e) => setProductoFiltro(e.target.value)} style={{ maxWidth: 280 }}>
            <option value="">Últimos 100 movimientos</option>
            {productos.datos?.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nombre}
              </option>
            ))}
          </select>
        }
      >
        <EstadoCarga cargando={movimientos.cargando} error={movimientos.error} onReintentar={movimientos.recargar}>
          {movimientos.datos?.length ? (
            <div className="tabla-contenedor">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Producto</th>
                    <th>Tipo</th>
                    <th>Cantidad</th>
                    <th>Saldo</th>
                    <th>Motivo</th>
                    <th>Usuario</th>
                  </tr>
                </thead>
                <tbody>
                  {movimientos.datos.map((x) => (
                    <tr key={x.id}>
                      <td>{formatoFechaHora(x.fecha)}</td>
                      <td>{x.productoNombre}</td>
                      <td>
                        <Insignia valor={x.tipo} />
                      </td>
                      <td>{x.cantidad}</td>
                      <td>{x.saldo}</td>
                      <td>{x.motivo}</td>
                      <td>{x.usuarioNombre}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <Vacio>Sin movimientos registrados.</Vacio>
          )}
        </EstadoCarga>
      </Tarjeta>
    </>
  );
}

const SIGUIENTE: Partial<Record<EstadoPedido, EstadoPedido>> = { PENDIENTE: 'CONFIRMADO', CONFIRMADO: 'DESPACHADO', DESPACHADO: 'ENTREGADO' };

/** Administracion de pedidos desde el panel (avance de estados y cancelacion). */
export function PedidosAdmin() {
  const [estado, setEstado] = useState<EstadoPedido | ''>('');
  const pedidos = useCarga(() => pedidosService.listar(estado), [estado]);
  const [detalle, setDetalle] = useState<Pedido | null>(null);
  const [cancelar, setCancelar] = useState<Pedido | null>(null);
  const m = useMensajes();
  return (
    <>
      <Encabezado titulo="Pedidos" migas={[{ texto: 'Panel', a: '/panel' }]} />
      {m.vista}
      <Tarjeta>
        <div className="filtros">
          <Campo etiqueta="Estado">
            <select value={estado} onChange={(e) => setEstado(e.target.value as EstadoPedido | '')}>
              <option value="">Todos</option>
              {(['PENDIENTE', 'CONFIRMADO', 'DESPACHADO', 'ENTREGADO', 'CANCELADO'] as EstadoPedido[]).map((x) => (
                <option key={x} value={x}>
                  {etiqueta(x)}
                </option>
              ))}
            </select>
          </Campo>
        </div>
        <EstadoCarga cargando={pedidos.cargando} error={pedidos.error} onReintentar={pedidos.recargar}>
          <TablaPedidos
            mostrarCliente
            pedidos={pedidos.datos ?? []}
            acciones={(p) => {
              const siguiente = SIGUIENTE[p.estado];
              return (
                <>
                  <button className="boton boton-secundario boton-pequeno" type="button" onClick={() => setDetalle(p)}>
                    Detalle
                  </button>
                  {siguiente && (
                    <button
                      className="boton boton-pequeno"
                      type="button"
                      onClick={async () => {
                        try {
                          await pedidosService.cambiarEstado(p.id, siguiente);
                          m.setMensaje(`Pedido ${p.numero}: ${etiquetaEstadoPedido({ ...p, estado: siguiente })}.`);
                          await pedidos.recargar();
                        } catch (e) {
                          m.setError(mensajeDeError(e));
                        }
                      }}
                    >
                      Marcar {etiquetaEstadoPedido({ ...p, estado: siguiente }).toLowerCase()}
                    </button>
                  )}
                  {(p.estado === 'PENDIENTE' || p.estado === 'CONFIRMADO') && (
                    <button className="boton boton-peligro boton-pequeno" type="button" onClick={() => setCancelar(p)}>
                      Cancelar
                    </button>
                  )}
                </>
              );
            }}
          />
        </EstadoCarga>
      </Tarjeta>
      {detalle && (
        <Modal titulo={`Pedido ${detalle.numero} · ${detalle.clienteNombre}`} onCerrar={() => setDetalle(null)}>
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
            m.setMensaje('Pedido cancelado; el stock fue repuesto.');
            await pedidos.recargar();
          }}
        />
      )}
    </>
  );
}

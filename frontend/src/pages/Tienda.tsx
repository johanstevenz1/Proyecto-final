import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Alerta, Campo, Cargando, Encabezado, Tarjeta, Vacio } from '../components/Comunes';
import { useAuth } from '../contexts/AuthContext';
import { useCarrito } from '../contexts/CarritoContext';
import { mensajeDeError, useCarga } from '../hooks/useCarga';
import type { CategoriaProducto, Cotizacion, ModalidadEntrega, Pedido } from '../interfaces/tipos';
import { clientesService, pedidosService, productosService } from '../services/servicios';
import { etiqueta, formatoMoneda } from '../utils/formato';

const CATEGORIAS: CategoriaProducto[] = ['MEDICAMENTOS', 'ALIMENTOS', 'ACCESORIOS', 'HIGIENE', 'JUGUETES'];

/** HU-16: catalogo con precio, stock, busqueda por nombre o categoria y carrito. */
export function Catalogo() {
  const { usuario } = useAuth();
  const { agregar, items } = useCarrito();
  const [texto, setTexto] = useState('');
  const [busqueda, setBusqueda] = useState('');
  const [categoria, setCategoria] = useState<CategoriaProducto | ''>('');
  const [cantidades, setCantidades] = useState<Record<string, number>>({});
  const [aviso, setAviso] = useState<string | null>(null);
  const productos = useCarga(() => productosService.catalogo(busqueda, categoria), [busqueda, categoria]);
  const puedeComprar = !usuario || usuario.rol !== 'VETERINARIO';

  useEffect(() => {
    const t = window.setTimeout(() => setBusqueda(texto), 350);
    return () => window.clearTimeout(t);
  }, [texto]);

  return (
    <div className="seccion" style={{ maxWidth: 1200 }}>
      <Encabezado titulo="Catálogo de productos" migas={[{ texto: 'Inicio', a: '/' }]} acciones={<Link className="boton" to="/carrito">Ver carrito ({items.length})</Link>} />
      {aviso && (
        <Alerta tipo="exito" onCerrar={() => setAviso(null)}>
          {aviso}
        </Alerta>
      )}
      <div className="filtros">
        <Campo etiqueta="Buscar por nombre">
          <input type="search" value={texto} onChange={(e) => setTexto(e.target.value)} placeholder="Ej. collar" />
        </Campo>
        <Campo etiqueta="Categoría">
          <select value={categoria} onChange={(e) => setCategoria(e.target.value as CategoriaProducto | '')}>
            <option value="">Todas</option>
            {CATEGORIAS.map((c) => (
              <option key={c} value={c}>
                {etiqueta(c)}
              </option>
            ))}
          </select>
        </Campo>
      </div>
      {productos.cargando ? (
        <Cargando />
      ) : productos.error ? (
        <Alerta tipo="error">{productos.error}</Alerta>
      ) : !productos.datos?.length ? (
        <Vacio>No hay productos que coincidan con la búsqueda.</Vacio>
      ) : (
        <div className="rejilla rejilla-auto">
          {productos.datos.map((p) => {
            const enCarrito = items.find((i) => i.productoId === p.id)?.cantidad ?? 0;
            const maximo = Math.max(0, Math.min(p.stock, 100) - enCarrito);
            const cantidad = Math.min(cantidades[p.id] ?? 1, Math.max(maximo, 1));
            return (
              <article key={p.id} className="tarjeta-item">
                <span className="insignia insignia-azul" style={{ alignSelf: 'flex-start' }}>
                  {etiqueta(p.categoria)}
                </span>
                <h3>{p.nombre}</h3>
                <p>{p.descripcion}</p>
                <div className="meta">
                  <span className="precio">{formatoMoneda(p.precio)}</span>
                  <span>{p.stock > 0 ? `${p.stock} disponibles` : 'Agotado'}</span>
                </div>
                {p.requiereFormula ? (
                  <Alerta tipo="aviso">Requiere fórmula veterinaria: solicítalo en el establecimiento.</Alerta>
                ) : puedeComprar ? (
                  <div className="meta">
                    <input
                      className="cantidad"
                      type="number"
                      min={1}
                      max={Math.max(maximo, 1)}
                      value={cantidad}
                      aria-label={`Cantidad de ${p.nombre}`}
                      onChange={(e) => setCantidades({ ...cantidades, [p.id]: Number(e.target.value) })}
                      disabled={maximo === 0}
                    />
                    <button
                      type="button"
                      className="boton"
                      disabled={maximo === 0 || cantidad < 1}
                      onClick={() => {
                        agregar(p, cantidad);
                        setAviso(`${p.nombre} agregado al carrito`);
                      }}
                    >
                      {maximo === 0 ? (p.stock === 0 ? 'Agotado' : 'Máximo en carrito') : 'Agregar al carrito'}
                    </button>
                  </div>
                ) : null}
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}

/** Carrito y confirmacion del pedido (HU-16, HU-17). El total lo calcula el backend. */
export function Carrito() {
  const { usuario } = useAuth();
  const { items, cambiarCantidad, quitar, vaciar } = useCarrito();
  const navegar = useNavigate();
  const [cotizacion, setCotizacion] = useState<Cotizacion | null>(null);
  const [errorCotizacion, setErrorCotizacion] = useState<string | null>(null);
  const [calculando, setCalculando] = useState(false);
  const [modalidad, setModalidad] = useState<ModalidadEntrega>('RECOGIDA');
  const [direccion, setDireccion] = useState('');
  const [clienteId, setClienteId] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [confirmado, setConfirmado] = useState<Pedido | null>(null);
  const esAdmin = usuario?.rol === 'ADMINISTRADOR';
  const clientes = useCarga(() => (esAdmin ? clientesService.buscar('', 0, 100, 'nombre') : Promise.resolve(null)), [esAdmin]);

  useEffect(() => {
    if (usuario?.rol === 'CLIENTE') {
      clientesService.perfil().then((p) => setDireccion((d) => d || p.cliente.direccion)).catch(() => undefined);
    }
  }, [usuario]);

  const claveItems = items.map((i) => `${i.productoId}:${i.cantidad}`).join('|');
  useEffect(() => {
    if (!usuario || !items.length || usuario.rol === 'VETERINARIO') {
      setCotizacion(null);
      return;
    }
    setCalculando(true);
    setErrorCotizacion(null);
    pedidosService
      .cotizar(items.map((i) => ({ productoId: i.productoId, cantidad: i.cantidad })))
      .then(setCotizacion)
      .catch((e) => {
        setCotizacion(null);
        setErrorCotizacion(mensajeDeError(e));
      })
      .finally(() => setCalculando(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [claveItems, usuario]);

  async function confirmar() {
    setError(null);
    setEnviando(true);
    try {
      const pedido = await pedidosService.crear(
        items.map((i) => ({ productoId: i.productoId, cantidad: i.cantidad })),
        modalidad,
        modalidad === 'DOMICILIO' ? direccion : '',
        esAdmin ? clienteId : undefined,
      );
      vaciar();
      setConfirmado(pedido);
    } catch (e) {
      setError(mensajeDeError(e));
    } finally {
      setEnviando(false);
    }
  }

  if (confirmado) {
    return (
      <div className="seccion" style={{ maxWidth: 800 }}>
        <Tarjeta titulo={`Pedido ${confirmado.numero} registrado`}>
          <Alerta tipo="exito">Tu pedido quedó registrado con estado “Pendiente”.</Alerta>
          <ResumenPedido pedido={confirmado} />
          <div className="acciones-formulario">
            <Link className="boton boton-secundario" to="/catalogo">
              Seguir comprando
            </Link>
            <button className="boton" type="button" onClick={() => navegar(usuario?.rol === 'CLIENTE' ? '/portal/pedidos' : '/panel/pedidos')}>
              Ver mis pedidos
            </button>
          </div>
        </Tarjeta>
      </div>
    );
  }

  return (
    <div className="seccion" style={{ maxWidth: 1000 }}>
      <Encabezado titulo="Carrito de compras" migas={[{ texto: 'Inicio', a: '/' }, { texto: 'Catálogo', a: '/catalogo' }]} />
      {!items.length ? (
        <Tarjeta>
          <Vacio>
            Tu carrito está vacío. <Link to="/catalogo">Ir al catálogo</Link>
          </Vacio>
        </Tarjeta>
      ) : (
        <>
          <Tarjeta titulo="Productos">
            <div className="tabla-contenedor">
              <table>
                <thead>
                  <tr>
                    <th>Producto</th>
                    <th>Precio</th>
                    <th>Cantidad</th>
                    <th>Subtotal</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {items.map((i) => {
                    const linea = cotizacion?.detalles.find((d) => d.productoId === i.productoId);
                    return (
                      <tr key={i.productoId}>
                        <td>{i.nombre}</td>
                        <td>{formatoMoneda(linea?.precioUnitario ?? i.precio)}</td>
                        <td>
                          <input
                            className="cantidad"
                            type="number"
                            min={1}
                            max={Math.min(i.stock, 100)}
                            value={i.cantidad}
                            aria-label={`Cantidad de ${i.nombre}`}
                            onChange={(e) => cambiarCantidad(i.productoId, Number(e.target.value))}
                          />
                        </td>
                        <td>{linea ? formatoMoneda(linea.subtotal) : '—'}</td>
                        <td>
                          <button type="button" className="boton boton-secundario boton-pequeno" onClick={() => quitar(i.productoId)}>
                            Eliminar
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            {!usuario ? (
              <Alerta tipo="info">
                <Link to="/login" state={{ desde: '/carrito' }}>
                  Inicia sesión
                </Link>{' '}
                para calcular el total y confirmar tu pedido.
              </Alerta>
            ) : usuario.rol === 'VETERINARIO' ? (
              <Alerta tipo="aviso">El personal veterinario no realiza pedidos.</Alerta>
            ) : calculando ? (
              <Cargando texto="Calculando total..." />
            ) : errorCotizacion ? (
              <Alerta tipo="error">{errorCotizacion}</Alerta>
            ) : cotizacion ? (
              <div className="resumen-pedido">
                <span>Total a pagar</span>
                <span>{formatoMoneda(cotizacion.total)}</span>
              </div>
            ) : null}
          </Tarjeta>

          {usuario && usuario.rol !== 'VETERINARIO' && (
            <Tarjeta titulo="Entrega">
              {error && <Alerta tipo="error">{error}</Alerta>}
              <div className="formulario">
                {esAdmin && (
                  <div className="completo">
                    <Campo etiqueta="Cliente del pedido" requerido>
                      <select value={clienteId} onChange={(e) => setClienteId(e.target.value)}>
                        <option value="">Selecciona un cliente</option>
                        {clientes.datos?.contenido.map((c) => (
                          <option key={c.id} value={c.id}>
                            {c.nombre} · {c.cedula}
                          </option>
                        ))}
                      </select>
                    </Campo>
                  </div>
                )}
                <Campo etiqueta="Modalidad de entrega" requerido>
                  <select value={modalidad} onChange={(e) => setModalidad(e.target.value as ModalidadEntrega)}>
                    <option value="RECOGIDA">{etiqueta('RECOGIDA')}</option>
                    <option value="DOMICILIO">{etiqueta('DOMICILIO')}</option>
                  </select>
                </Campo>
                {modalidad === 'DOMICILIO' && (
                  <Campo etiqueta="Dirección de entrega" requerido ayuda={esAdmin ? 'Si se deja vacía se usa la del cliente' : undefined}>
                    <input value={direccion} onChange={(e) => setDireccion(e.target.value)} maxLength={200} />
                  </Campo>
                )}
                <div className="acciones-formulario">
                  <button type="button" className="boton boton-secundario" onClick={vaciar}>
                    Vaciar carrito
                  </button>
                  <button
                    type="button"
                    className="boton boton-exito"
                    disabled={enviando || !cotizacion || (esAdmin && !clienteId) || (modalidad === 'DOMICILIO' && !esAdmin && !direccion.trim())}
                    onClick={confirmar}
                  >
                    {enviando ? 'Confirmando...' : 'Confirmar pedido'}
                  </button>
                </div>
              </div>
            </Tarjeta>
          )}
        </>
      )}
    </div>
  );
}

export function ResumenPedido({ pedido }: { pedido: Pedido }) {
  return (
    <>
      <p>
        <strong>{etiqueta(pedido.modalidad)}</strong>
        {pedido.direccionEntrega ? ` · ${pedido.direccionEntrega}` : ''}
      </p>
      <div className="tabla-contenedor">
        <table>
          <thead>
            <tr>
              <th>Producto</th>
              <th>Cantidad</th>
              <th>Precio</th>
              <th>Subtotal</th>
            </tr>
          </thead>
          <tbody>
            {pedido.detalles.map((d) => (
              <tr key={d.productoId}>
                <td>{d.nombreProducto}</td>
                <td>{d.cantidad}</td>
                <td>{formatoMoneda(d.precioUnitario)}</td>
                <td>{formatoMoneda(d.subtotal)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="resumen-pedido">
        <span>Total</span>
        <span>{formatoMoneda(pedido.total)}</span>
      </div>
    </>
  );
}

import { Link } from 'react-router-dom';
import { Alerta, EstadoCarga, Insignia, Tarjeta, Vacio } from '../../components/Comunes';
import { BarrasHorizontales, EjeBarras, GraficaBarras, GraficaDona } from '../../components/Graficas';
import { IconoAlerta, IconoBolsa, IconoCalendario, IconoCaja, IconoFlecha, IconoGrafica, IconoHuella, IconoMas, IconoUsuarios } from '../../components/Iconos';
import { useAuth } from '../../contexts/AuthContext';
import { useCarga } from '../../hooks/useCarga';
import type { Especie } from '../../interfaces/tipos';
import { citasService, panelService } from '../../services/servicios';
import { etiqueta, formatoFecha, formatoHora, formatoMoneda, hoyIso } from '../../utils/formato';
import { etiquetaEstadoPedido } from '../cliente/PortalCliente';

const diaCorto = (iso: string) => {
  const [a, m, d] = iso.split('-').map(Number);
  return new Date(a, m - 1, d).toLocaleDateString('es-CO', { weekday: 'short', day: 'numeric' });
};
const monedaCorta = (v: number) => (v >= 1000000 ? `$${(v / 1000000).toFixed(1)}M` : v >= 1000 ? `$${Math.round(v / 1000)}k` : `$${v}`);

/** HU-24: dashboard del personal con metricas, graficas y accesos rapidos. */
export function Dashboard() {
  const { usuario, tieneRol } = useAuth();
  const esAdmin = tieneRol('ADMINISTRADOR');
  const panel = useCarga(() => Promise.all([panelService.resumen(), citasService.listar({ desde: hoyIso(), hasta: hoyIso() })]), []);
  const [r, citasHoy] = panel.datos ?? [null, []];
  const totalHoy = r ? Object.values(r.citasHoy).reduce((a, b) => a + (b ?? 0), 0) : 0;

  return (
    <EstadoCarga cargando={panel.cargando} error={panel.error} onReintentar={panel.recargar}>
      {r && (
        <>
          <section className="banner-bienvenida banner-panel">
            <div>
              <span className="banner-fecha">{new Date().toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</span>
              <h1>Buen día, {usuario?.nombre.split(' ')[0]}</h1>
              <p>
                Hoy hay {totalHoy} citas en agenda, {r.citasPendientes} solicitudes por confirmar y {r.alertasStock.length} alertas de inventario.
              </p>
            </div>
            <div className="acciones">
              <Link className="boton boton-claro" to="/panel/citas">
                <IconoCalendario tam={18} /> Revisar agenda
              </Link>
              {esAdmin && (
                <Link className="boton boton-transparente" to="/panel/clientes">
                  <IconoMas tam={18} /> Registrar cliente
                </Link>
              )}
            </div>
          </section>

          <div className="rejilla rejilla-4 metricas">
            <Link to="/panel/clientes" className="metrica-icono">
              <span className="icono-circulo azul">
                <IconoUsuarios />
              </span>
              <div>
                <strong>{r.totalClientes}</strong>
                <span>Clientes activos</span>
              </div>
            </Link>
            <Link to="/panel/mascotas" className="metrica-icono">
              <span className="icono-circulo verde">
                <IconoHuella />
              </span>
              <div>
                <strong>{r.totalMascotas}</strong>
                <span>Mascotas registradas</span>
              </div>
            </Link>
            <Link to="/panel/citas" className="metrica-icono">
              <span className="icono-circulo naranja">
                <IconoCalendario />
              </span>
              <div>
                <strong>{totalHoy}</strong>
                <span>
                  Citas hoy · {r.citasHoy.PENDIENTE ?? 0} pend. · {r.citasHoy.CONFIRMADA ?? 0} conf. · {r.citasHoy.ATENDIDA ?? 0} atend.
                </span>
              </div>
            </Link>
            <Link to={esAdmin ? '/panel/pedidos' : '/panel/inventario'} className="metrica-icono">
              <span className="icono-circulo morado">{esAdmin ? <IconoGrafica /> : <IconoCaja />}</span>
              <div>
                <strong>{esAdmin ? formatoMoneda(r.ventasMes) : r.alertasStock.length}</strong>
                <span>{esAdmin ? `Ventas del mes · ${r.pedidosPendientes} pedidos pendientes` : 'Alertas de stock'}</span>
              </div>
            </Link>
          </div>

          {r.citasPendientes > 0 && (
            <Alerta tipo="aviso">
              Hay {r.citasPendientes} citas pendientes de confirmación. <Link to="/panel/citas">Revisar solicitudes</Link>
            </Alerta>
          )}

          <div className="rejilla rejilla-2">
            <Tarjeta titulo="Citas de los próximos 7 días">
              <GraficaBarras datos={r.citasProximosDias.map((d) => ({ etiqueta: diaCorto(d.fecha), valor: d.cantidad }))} />
              <EjeBarras datos={r.citasProximosDias.map((d) => ({ etiqueta: diaCorto(d.fecha), valor: d.cantidad }))} />
            </Tarjeta>
            {esAdmin ? (
              <Tarjeta titulo="Ventas de los últimos 7 días">
                <GraficaBarras color="var(--verde)" datos={r.ventasUltimosDias.map((d) => ({ etiqueta: diaCorto(d.fecha), valor: d.valor ?? 0, detalle: `${d.cantidad} pedidos · ${formatoMoneda(d.valor ?? 0)}` }))} />
                <EjeBarras formato={monedaCorta} datos={r.ventasUltimosDias.map((d) => ({ etiqueta: diaCorto(d.fecha), valor: d.valor ?? 0 }))} />
              </Tarjeta>
            ) : (
              <Tarjeta titulo="Mascotas por especie">
                <GraficaDona datos={(Object.entries(r.mascotasPorEspecie) as [Especie, number][]).map(([k, v]) => ({ etiqueta: etiqueta(k), valor: v }))} />
              </Tarjeta>
            )}
          </div>

          <div className="rejilla rejilla-3">
            <Tarjeta titulo="Citas de hoy" acciones={<Link to="/panel/citas">Agenda</Link>}>
              {citasHoy.length ? (
                <ul className="lista-citas">
                  {citasHoy.map((c) => (
                    <li key={c.id}>
                      <div className="fecha-bloque">
                        <b>{formatoHora(c.inicio)}</b>
                      </div>
                      <div>
                        <strong>{c.mascotaNombre}</strong>
                        <small>
                          {c.servicioNombre} · {c.clienteNombre}
                        </small>
                      </div>
                      <Insignia valor={c.estado} />
                    </li>
                  ))}
                </ul>
              ) : (
                <Vacio>No hay citas programadas para hoy.</Vacio>
              )}
            </Tarjeta>
            {esAdmin && (
              <Tarjeta titulo="Mascotas por especie">
                <GraficaDona datos={(Object.entries(r.mascotasPorEspecie) as [Especie, number][]).map(([k, v]) => ({ etiqueta: etiqueta(k), valor: v }))} />
              </Tarjeta>
            )}
            <Tarjeta
              titulo={
                <span className="titulo-alerta">
                  <IconoAlerta tam={18} /> Alertas de stock
                </span>
              }
              acciones={<Link to="/panel/inventario">Inventario</Link>}
            >
              {r.alertasStock.length ? (
                <ul className="lista-alertas">
                  {r.alertasStock.map((p) => (
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

          {esAdmin && (
            <div className="rejilla rejilla-2">
              <Tarjeta titulo="Productos más vendidos del mes">
                {r.productosMasVendidos.length ? (
                  <BarrasHorizontales sufijo=" uds" datos={r.productosMasVendidos.map((p) => ({ etiqueta: p.nombre, valor: p.cantidad }))} />
                ) : (
                  <Vacio>Aún no hay ventas este mes.</Vacio>
                )}
              </Tarjeta>
              <Tarjeta titulo="Pedidos recientes" acciones={<Link to="/panel/pedidos">Ver pedidos</Link>}>
                {r.pedidosRecientes.length ? (
                  <ul className="lista-citas">
                    {r.pedidosRecientes.map((p) => (
                      <li key={p.id}>
                        <span className="icono-circulo morado">
                          <IconoBolsa />
                        </span>
                        <div>
                          <strong>
                            {p.numero} · {formatoMoneda(p.total)}
                          </strong>
                          <small>
                            {p.clienteNombre} · {formatoFecha(p.fecha)}
                          </small>
                        </div>
                        <Insignia valor={p.estado} texto={etiquetaEstadoPedido(p)} />
                      </li>
                    ))}
                  </ul>
                ) : (
                  <Vacio>Aún no hay pedidos.</Vacio>
                )}
              </Tarjeta>
            </div>
          )}

          <Tarjeta titulo="Accesos rápidos">
            <div className="accesos-rapidos">
              {[
                { a: '/panel/citas', t: 'Confirmar citas', i: IconoCalendario, admin: false },
                { a: '/panel/mascotas', t: 'Historial clínico', i: IconoHuella, admin: false },
                { a: '/panel/inventario', t: 'Movimientos de stock', i: IconoCaja, admin: false },
                { a: '/panel/productos', t: 'Productos', i: IconoGrafica, admin: true },
                { a: '/panel/usuarios', t: 'Roles de usuarios', i: IconoUsuarios, admin: true },
              ]
                .filter((x) => !x.admin || esAdmin)
                .map((x) => (
                  <Link key={x.a} to={x.a}>
                    <x.i /> {x.t} <IconoFlecha tam={14} />
                  </Link>
                ))}
            </div>
          </Tarjeta>
        </>
      )}
    </EstadoCarga>
  );
}

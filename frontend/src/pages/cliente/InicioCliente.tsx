import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Alerta, EstadoCarga, Insignia, Tarjeta, Vacio } from '../../components/Comunes';
import { IconoBolsa, IconoCalendario, IconoCarrito, IconoFlecha, IconoHuella, IconoJeringa, IconoMas, IconoReloj, emojiEspecie } from '../../components/Iconos';
import { useAuth } from '../../contexts/AuthContext';
import { useCarga } from '../../hooks/useCarga';
import type { Atencion, Mascota } from '../../interfaces/tipos';
import { citasService, mascotasService, pedidosService } from '../../services/servicios';
import { ahoraIsoLocal, edad, etiqueta, formatoFecha, formatoFechaHora, formatoHora, formatoMoneda, hoyIso } from '../../utils/formato';
import { etiquetaEstadoPedido } from './PortalCliente';

interface Recordatorio {
  mascota: Mascota;
  atencion: Atencion;
}

/** Dias que faltan para una fecha (negativo si ya paso). */
function diasHasta(iso: string): number {
  const [a, m, d] = iso.split('-').map(Number);
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  return Math.round((new Date(a, m - 1, d).getTime() - hoy.getTime()) / 86400000);
}

/** Dashboard del cliente: mascotas, proxima cita, recordatorios de salud y pedidos. */
export function PortalCliente() {
  const { usuario } = useAuth();
  const ubicacion = useLocation();
  const [bienvenida, setBienvenida] = useState(!!(ubicacion.state as { bienvenida?: boolean } | null)?.bienvenida);
  const carga = useCarga(async () => {
    const [mascotas, citas, pedidos] = await Promise.all([mascotasService.listar(), citasService.listar(), pedidosService.listar()]);
    const historiales = await Promise.all(mascotas.map((m) => mascotasService.historial(m.id).catch(() => [] as Atencion[])));
    return { mascotas, citas, pedidos, historiales };
  }, []);

  const mascotas = carga.datos?.mascotas ?? [];
  const citas = carga.datos?.citas ?? [];
  const pedidos = carga.datos?.pedidos ?? [];
  const historiales = carga.datos?.historiales ?? [];
  const ahora = ahoraIsoLocal();
  const proximas = citas.filter((c) => (c.estado === 'PENDIENTE' || c.estado === 'CONFIRMADA') && c.inicio > ahora).sort((a, b) => a.inicio.localeCompare(b.inicio));
  const siguiente = proximas[0];
  const enCurso = pedidos.filter((p) => ['PENDIENTE', 'CONFIRMADO', 'DESPACHADO'].includes(p.estado));

  // Proxima dosis o control de cada mascota
  const recordatorios: Recordatorio[] = mascotas
    .flatMap((m, i) => (historiales[i] ?? []).filter((a) => a.proximaFecha).map((a) => ({ mascota: m, atencion: a })))
    .filter((r) => diasHasta(r.atencion.proximaFecha!) >= -30)
    .sort((a, b) => a.atencion.proximaFecha!.localeCompare(b.atencion.proximaFecha!));
  const proximaDosisDe = (m: Mascota) => recordatorios.find((r) => r.mascota.id === m.id && r.atencion.proximaFecha! >= hoyIso());

  return (
    <>
      {bienvenida && (
        <Alerta tipo="exito" onCerrar={() => setBienvenida(false)}>
          Tu cuenta fue creada correctamente. Registra a tu mascota para solicitar citas.
        </Alerta>
      )}
      <EstadoCarga cargando={carga.cargando} error={carga.error} onReintentar={carga.recargar}>
        <section className="banner-bienvenida">
          <div>
            <span className="banner-fecha">{new Date().toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' })}</span>
            <h1>Hola, {usuario?.nombre.split(' ')[0]} 👋</h1>
            <p>
              {siguiente
                ? `Tu próxima cita es ${formatoFechaHora(siguiente.inicio).toLowerCase()} para ${siguiente.mascotaNombre}.`
                : mascotas.length
                  ? 'No tienes citas próximas. ¿Agendamos una?'
                  : 'Empieza registrando a tu mascota.'}
            </p>
            <div className="acciones">
              <Link className="boton boton-claro" to="/portal/citas/nueva">
                <IconoCalendario tam={18} /> Solicitar cita
              </Link>
              <Link className="boton boton-transparente" to="/catalogo">
                <IconoCarrito tam={18} /> Ir a la tienda
              </Link>
            </div>
          </div>
          {siguiente && (
            <div className="tarjeta-cita-destacada">
              <span className="etiqueta-pequena">Próxima cita</span>
              <strong>{siguiente.servicioNombre}</strong>
              <span>
                <IconoCalendario tam={16} /> {formatoFecha(siguiente.inicio)}
              </span>
              <span>
                <IconoReloj tam={16} /> {formatoHora(siguiente.inicio)} - {formatoHora(siguiente.fin)}
              </span>
              <span>
                {emojiEspecie(mascotas.find((m) => m.id === siguiente.mascotaId)?.especie ?? '')} {siguiente.mascotaNombre}
              </span>
              <Insignia valor={siguiente.estado} />
            </div>
          )}
        </section>

        <div className="rejilla rejilla-4 metricas">
          <Link to="/portal/mascotas" className="metrica-icono">
            <span className="icono-circulo verde">
              <IconoHuella />
            </span>
            <div>
              <strong>{mascotas.length}</strong>
              <span>Mascotas</span>
            </div>
          </Link>
          <Link to="/portal/citas" className="metrica-icono">
            <span className="icono-circulo azul">
              <IconoCalendario />
            </span>
            <div>
              <strong>{proximas.length}</strong>
              <span>Citas próximas</span>
            </div>
          </Link>
          <Link to="/portal/mascotas" className="metrica-icono">
            <span className="icono-circulo naranja">
              <IconoJeringa />
            </span>
            <div>
              <strong>{recordatorios.filter((r) => r.atencion.proximaFecha! >= hoyIso()).length}</strong>
              <span>Dosis programadas</span>
            </div>
          </Link>
          <Link to="/portal/pedidos" className="metrica-icono">
            <span className="icono-circulo morado">
              <IconoBolsa />
            </span>
            <div>
              <strong>{enCurso.length}</strong>
              <span>Pedidos en curso</span>
            </div>
          </Link>
        </div>

        <Tarjeta
          titulo="Mis mascotas"
          acciones={
            <Link className="boton boton-secundario boton-pequeno" to="/portal/mascotas">
              <IconoMas tam={16} /> Registrar mascota
            </Link>
          }
        >
          {mascotas.length ? (
            <div className="rejilla rejilla-auto">
              {mascotas.map((m) => {
                const dosis = proximaDosisDe(m);
                return (
                  <Link key={m.id} to={`/portal/mascotas/${m.id}`} className="tarjeta-mascota">
                    <span className="avatar-mascota">{emojiEspecie(m.especie)}</span>
                    <div>
                      <h3>{m.nombre}</h3>
                      <p>
                        {etiqueta(m.especie)} · {m.raza} · {etiqueta(m.sexo)}
                      </p>
                      <div className="chips">
                        <span>{edad(m.fechaNacimiento)}</span>
                        <span>{m.peso} kg</span>
                      </div>
                      {dosis && (
                        <small className="proxima-dosis">
                          <IconoJeringa tam={14} /> {etiqueta(dosis.atencion.tipo)}: {formatoFecha(dosis.atencion.proximaFecha)}
                        </small>
                      )}
                    </div>
                  </Link>
                );
              })}
            </div>
          ) : (
            <Vacio>
              Aún no registras mascotas. <Link to="/portal/mascotas">Registrar la primera</Link>
            </Vacio>
          )}
        </Tarjeta>

        <div className="rejilla rejilla-2">
          <Tarjeta titulo="Recordatorios de salud">
            {recordatorios.length ? (
              <ul className="lista-recordatorios">
                {recordatorios.slice(0, 6).map((r) => {
                  const dias = diasHasta(r.atencion.proximaFecha!);
                  return (
                    <li key={r.atencion.id} className={dias < 0 ? 'vencido' : dias <= 30 ? 'cercano' : ''}>
                      <span className="avatar-mascota pequeno">{emojiEspecie(r.mascota.especie)}</span>
                      <div>
                        <strong>
                          {r.mascota.nombre}: {r.atencion.descripcion}
                        </strong>
                        <small>
                          {etiqueta(r.atencion.tipo)} · {formatoFecha(r.atencion.proximaFecha)}
                        </small>
                      </div>
                      <span className="dias">{dias < 0 ? `Vencido hace ${-dias} d` : dias === 0 ? 'Hoy' : `En ${dias} d`}</span>
                    </li>
                  );
                })}
              </ul>
            ) : (
              <Vacio>No hay vacunas ni controles programados.</Vacio>
            )}
          </Tarjeta>
          <Tarjeta titulo="Próximas citas" acciones={<Link to="/portal/citas">Ver todas</Link>}>
            {proximas.length ? (
              <ul className="lista-citas">
                {proximas.slice(0, 5).map((c) => (
                  <li key={c.id}>
                    <div className="fecha-bloque">
                      <b>{c.inicio.slice(8, 10)}</b>
                      <span>{new Date(c.inicio).toLocaleDateString('es-CO', { month: 'short' })}</span>
                    </div>
                    <div>
                      <strong>{c.servicioNombre}</strong>
                      <small>
                        {formatoHora(c.inicio)} · {c.mascotaNombre}
                        {c.veterinarioNombre ? ` · ${c.veterinarioNombre}` : ''}
                      </small>
                    </div>
                    <Insignia valor={c.estado} />
                  </li>
                ))}
              </ul>
            ) : (
              <Vacio>No tienes citas próximas.</Vacio>
            )}
          </Tarjeta>
        </div>

        <Tarjeta titulo="Pedidos recientes" acciones={<Link to="/portal/pedidos">Ver todos</Link>}>
          {pedidos.length ? (
            <ul className="lista-citas">
              {pedidos.slice(0, 4).map((p) => (
                <li key={p.id}>
                  <span className="icono-circulo morado">
                    <IconoBolsa />
                  </span>
                  <div>
                    <strong>
                      {p.numero} · {formatoMoneda(p.total)}
                    </strong>
                    <small>
                      {formatoFecha(p.fecha)} · {p.detalles.length} producto(s) · {etiqueta(p.modalidad)}
                    </small>
                  </div>
                  <Insignia valor={p.estado} texto={etiquetaEstadoPedido(p)} />
                </li>
              ))}
            </ul>
          ) : (
            <Vacio>
              Aún no has hecho pedidos.{' '}
              <Link to="/catalogo">
                Ver tienda <IconoFlecha tam={14} />
              </Link>
            </Vacio>
          )}
        </Tarjeta>
      </EstadoCarga>
    </>
  );
}

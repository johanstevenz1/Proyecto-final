import { Link } from 'react-router-dom';
import { Cargando } from '../components/Comunes';
import {
  IconoCalendario,
  IconoCamion,
  IconoCarrito,
  IconoEscudo,
  IconoEstetoscopio,
  IconoFlecha,
  IconoHuella,
  IconoJeringa,
  IconoPortapapeles,
  IconoTijeras,
} from '../components/Iconos';
import { inicioPorRol, useAuth } from '../contexts/AuthContext';
import { useCarga } from '../hooks/useCarga';
import { productosService, serviciosService } from '../services/servicios';
import { etiqueta, formatoMoneda } from '../utils/formato';

/** Pagina de inicio: presenta Jovan Pets, sus servicios y accesos a las funcionalidades. */
export function Inicio() {
  const { usuario } = useAuth();
  const datos = useCarga(() => Promise.all([serviciosService.activos(), productosService.catalogo()]), []);
  const [servicios, productos] = datos.datos ?? [[], []];
  const destino = usuario ? inicioPorRol(usuario.rol) : '/registro';

  const modulos = [
    { icono: IconoEstetoscopio, titulo: 'Atención veterinaria', texto: 'Consultas, controles, vacunas y desparasitación con agenda en línea.', a: usuario?.rol === 'CLIENTE' ? '/portal/citas/nueva' : destino },
    { icono: IconoTijeras, titulo: 'Peluquería', texto: 'Baño, corte y cuidado estético con una agenda independiente.', a: usuario?.rol === 'CLIENTE' ? '/portal/citas/nueva' : destino },
    { icono: IconoPortapapeles, titulo: 'Historial clínico', texto: 'Antecedentes, vacunas y próximas dosis de cada mascota.', a: usuario?.rol === 'CLIENTE' ? '/portal/mascotas' : destino },
    { icono: IconoCarrito, titulo: 'Tienda en línea', texto: 'Alimentos, accesorios e higiene con entrega o recogida.', a: '/catalogo' },
  ];

  return (
    <>
      <section className="portada">
        <div className="portada-contenido">
          <div>
            <span className="etiqueta-portada">
              <IconoHuella tam={16} /> Veterinaria · Peluquería · Tienda
            </span>
            <h1>
              Cuidamos a tu mascota <span>como parte de la familia</span>
            </h1>
            <p>Agenda citas en minutos, consulta el historial de salud de tus mascotas y recibe sus productos en casa. Todo en un solo lugar.</p>
            <div className="acciones">
              {usuario ? (
                <Link className="boton boton-grande boton-claro" to={inicioPorRol(usuario.rol)}>
                  Ir a mi {usuario.rol === 'CLIENTE' ? 'portal' : 'panel'} <IconoFlecha tam={18} />
                </Link>
              ) : (
                <>
                  <Link className="boton boton-grande boton-claro" to="/registro">
                    Crear mi cuenta <IconoFlecha tam={18} />
                  </Link>
                  <Link className="boton boton-grande boton-transparente" to="/login">
                    Iniciar sesión
                  </Link>
                </>
              )}
            </div>
            <div className="portada-cifras">
              <div>
                <strong>{servicios.length}</strong>
                <span>servicios disponibles</span>
              </div>
              <div>
                <strong>{productos.length}</strong>
                <span>productos en tienda</span>
              </div>
              <div>
                <strong>2</strong>
                <span>agendas: veterinaria y peluquería</span>
              </div>
            </div>
          </div>
          <div className="portada-tarjetas" aria-hidden="true">
            <div className="tarjeta-flotante t1">
              <span className="icono-circulo azul">
                <IconoCalendario />
              </span>
              <div>
                <strong>Cita confirmada</strong>
                <small>Consulta general · 9:00 a. m.</small>
              </div>
            </div>
            <div className="mascota-grande">🐶</div>
            <div className="tarjeta-flotante t2">
              <span className="icono-circulo verde">
                <IconoJeringa />
              </span>
              <div>
                <strong>Próxima vacuna</strong>
                <small>Recordatorio en tu portal</small>
              </div>
            </div>
            <div className="tarjeta-flotante t3">
              <span className="icono-circulo naranja">
                <IconoCamion />
              </span>
              <div>
                <strong>Pedido en camino</strong>
                <small>Entrega a domicilio</small>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="seccion">
        <div className="seccion-titulo">
          <span>Qué hacemos</span>
          <h2>Todo lo que tu mascota necesita</h2>
        </div>
        <div className="rejilla rejilla-4">
          {modulos.map((m) => (
            <Link key={m.titulo} to={m.a} className="tarjeta-modulo">
              <span className="icono-circulo azul">
                <m.icono tam={24} />
              </span>
              <h3>{m.titulo}</h3>
              <p>{m.texto}</p>
              <span className="enlace-flecha">
                Ir <IconoFlecha tam={16} />
              </span>
            </Link>
          ))}
        </div>
      </section>

      <section className="seccion seccion-gris" id="servicios">
        <div className="seccion-titulo">
          <span>Servicios</span>
          <h2>Precios claros y duración estimada</h2>
        </div>
        {datos.cargando ? (
          <Cargando />
        ) : servicios.length ? (
          <div className="rejilla rejilla-auto">
            {servicios.map((s) => (
              <article key={s.id} className={`tarjeta-servicio ${s.categoria === 'PELUQUERIA' ? 'peluqueria' : ''}`}>
                <span className="icono-circulo">{s.categoria === 'PELUQUERIA' ? <IconoTijeras /> : <IconoEstetoscopio />}</span>
                <span className="insignia insignia-azul">{etiqueta(s.categoria)}</span>
                <h3>{s.nombre}</h3>
                <p>{s.descripcion}</p>
                <div className="meta">
                  <span className="precio">{formatoMoneda(s.precio)}</span>
                  <span>{s.duracionMinutos} min</span>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <p className="vacio">Pronto publicaremos nuestros servicios.</p>
        )}
      </section>

      {productos.length > 0 && (
        <section className="seccion">
          <div className="seccion-titulo con-accion">
            <div>
              <span>Tienda</span>
              <h2>Productos destacados</h2>
            </div>
            <Link className="boton boton-secundario" to="/catalogo">
              Ver toda la tienda <IconoFlecha tam={16} />
            </Link>
          </div>
          <div className="rejilla rejilla-4">
            {productos.slice(0, 4).map((p) => (
              <Link key={p.id} to="/catalogo" className="tarjeta-producto">
                <div className="producto-imagen">{iconoCategoria(p.categoria)}</div>
                <span className="insignia insignia-azul">{etiqueta(p.categoria)}</span>
                <h3>{p.nombre}</h3>
                <span className="precio">{formatoMoneda(p.precio)}</span>
              </Link>
            ))}
          </div>
        </section>
      )}

      <section className="seccion">
        <div className="seccion-titulo">
          <span>Cómo funciona</span>
          <h2>En tres pasos</h2>
        </div>
        <div className="rejilla rejilla-3 pasos">
          <div>
            <b>1</b>
            <h3>Crea tu cuenta</h3>
            <p>Regístrate con tus datos y agrega a tus mascotas.</p>
          </div>
          <div>
            <b>2</b>
            <h3>Agenda o compra</h3>
            <p>Elige servicio y horario disponible, o arma tu carrito.</p>
          </div>
          <div>
            <b>3</b>
            <h3>Haz seguimiento</h3>
            <p>Consulta estados de citas y pedidos y el historial de salud.</p>
          </div>
        </div>
      </section>

      <section className="franja-cta">
        <div>
          <IconoEscudo tam={38} />
          <div>
            <h2>Tu mascota, en buenas manos</h2>
            <p>Información protegida y acceso según tu rol.</p>
          </div>
        </div>
        <Link className="boton boton-grande boton-claro" to={usuario ? inicioPorRol(usuario.rol) : '/registro'}>
          {usuario ? 'Continuar' : 'Empezar ahora'} <IconoFlecha tam={18} />
        </Link>
      </section>
    </>
  );
}

export function iconoCategoria(categoria: string): string {
  return { MEDICAMENTOS: '💊', ALIMENTOS: '🥫', ACCESORIOS: '🦮', HIGIENE: '🧴', JUGUETES: '🎾' }[categoria] ?? '🐾';
}

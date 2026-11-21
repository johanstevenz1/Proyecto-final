import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Cargando, Encabezado, Modal } from '../components/Comunes';
import {
  IconoCalendario,
  IconoCamion,
  IconoCarrito,
  IconoEscudo,
  IconoEstetoscopio,
  IconoFlecha,
  IconoHuella,
  IconoJeringa,
  IconoMas,
  IconoPortapapeles,
  IconoTijeras,
  IconoUsuario,
} from '../components/Iconos';
import { inicioPorRol, useAuth } from '../contexts/AuthContext';
import { useCarga } from '../hooks/useCarga';
import { serviciosService } from '../services/servicios';
import { etiqueta, formatoMoneda } from '../utils/formato';

/** Pagina de inicio: presenta Jovan Pets y da acceso a iniciar sesion o registrarse. */
export function Inicio() {
  const { usuario } = useAuth();
  const [acceso, setAcceso] = useState<{ titulo: string; destino: string } | null>(null);
  const esCliente = usuario?.rol === 'CLIENTE';
  const panel = usuario ? inicioPorRol(usuario.rol) : '/login';

  // Con sesion iniciada cada tarjeta lleva a su funcion; sin sesion se ofrece entrar o registrarse.
  const modulos = [
    { icono: IconoEstetoscopio, titulo: 'Atención veterinaria', texto: 'Consultas, controles, vacunas y desparasitación con agenda en línea.', a: esCliente ? '/portal/citas/nueva' : panel },
    { icono: IconoTijeras, titulo: 'Peluquería', texto: 'Baño, corte y cuidado estético con una agenda independiente.', a: esCliente ? '/portal/citas/nueva' : panel },
    { icono: IconoPortapapeles, titulo: 'Historial clínico', texto: 'Antecedentes, vacunas y próximas dosis de cada mascota.', a: esCliente ? '/portal/mascotas' : usuario ? '/panel/mascotas' : panel },
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
                  <Link className="boton boton-grande boton-claro" to="/login">
                    Iniciar sesión <IconoFlecha tam={18} />
                  </Link>
                  <Link className="boton boton-grande boton-transparente" to="/registro">
                    Crear cuenta
                  </Link>
                </>
              )}
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
          {modulos.map((m) => {
            const contenido = (
              <>
                <span className="icono-circulo azul">
                  <m.icono tam={24} />
                </span>
                <h3>{m.titulo}</h3>
                <p>{m.texto}</p>
                <span className="enlace-flecha">
                  Ir <IconoFlecha tam={16} />
                </span>
              </>
            );
            return usuario ? (
              <Link key={m.titulo} to={m.a} className="tarjeta-modulo">
                {contenido}
              </Link>
            ) : (
              <button key={m.titulo} type="button" className="tarjeta-modulo" onClick={() => setAcceso({ titulo: m.titulo, destino: m.a })}>
                {contenido}
              </button>
            );
          })}
        </div>
      </section>

      {acceso && (
        <Modal titulo={acceso.titulo} onCerrar={() => setAcceso(null)}>
          <p className="ayuda">Para continuar inicia sesión con tu cuenta o crea una nueva.</p>
          <div className="opciones-acceso">
            <Link className="opcion-acceso" to="/login" state={{ desde: acceso.destino === '/login' ? undefined : acceso.destino }}>
              <span className="icono-circulo azul">
                <IconoUsuario />
              </span>
              <span>
                <strong>Iniciar sesión</strong>
                <small>Ya tengo una cuenta en Jovan Pets</small>
              </span>
              <IconoFlecha tam={18} />
            </Link>
            <Link className="opcion-acceso" to="/registro">
              <span className="icono-circulo verde">
                <IconoMas />
              </span>
              <span>
                <strong>Crear cuenta</strong>
                <small>Soy nuevo y quiero registrarme</small>
              </span>
              <IconoFlecha tam={18} />
            </Link>
          </div>
        </Modal>
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

/** Servicios con precio y duracion (visible solo con sesion iniciada). */
export function Servicios() {
  const { usuario } = useAuth();
  const servicios = useCarga(() => serviciosService.activos(), []);
  return (
    <div className="seccion">
      <Encabezado
        titulo="Servicios"
        migas={[{ texto: 'Inicio', a: '/' }]}
        acciones={
          usuario?.rol === 'CLIENTE' && (
            <Link className="boton" to="/portal/citas/nueva">
              <IconoCalendario tam={18} /> Solicitar cita
            </Link>
          )
        }
      />
      {servicios.cargando ? (
        <Cargando />
      ) : servicios.error ? (
        <p className="vacio">{servicios.error}</p>
      ) : servicios.datos?.length ? (
        <div className="rejilla rejilla-auto">
          {servicios.datos.map((s) => (
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
        <p className="vacio">Aún no hay servicios publicados.</p>
      )}
    </div>
  );
}

export function iconoCategoria(categoria: string): string {
  return { MEDICAMENTOS: '💊', ALIMENTOS: '🥫', ACCESORIOS: '🦮', HIGIENE: '🧴', JUGUETES: '🎾' }[categoria] ?? '🐾';
}

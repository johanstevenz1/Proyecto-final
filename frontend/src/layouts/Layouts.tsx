import { useEffect, useState, type ComponentType } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import {
  IconoBolsa,
  IconoCaja,
  IconoCalendario,
  IconoCampana,
  IconoCarrito,
  IconoEtiqueta,
  IconoGrafica,
  IconoHuella,
  IconoInicio,
  IconoPortapapeles,
  IconoSalir,
  IconoTablero,
  IconoUsuario,
  IconoUsuarios,
} from '../components/Iconos';
import logo from '../assets/logo.svg';
import { inicioPorRol, useAuth } from '../contexts/AuthContext';
import { useCarrito } from '../contexts/CarritoContext';
import type { Rol } from '../interfaces/tipos';
import { citasService } from '../services/servicios';
import { etiqueta } from '../utils/formato';

interface Enlace {
  a: string;
  texto: string;
  roles: Rol[];
  icono: ComponentType<{ tam?: number }>;
  seccion: string;
}

// Menu segun la matriz de permisos por rol (REQUERIMIENTOS 4.3).
const MENU: Enlace[] = [
  { a: '/portal', texto: 'Mi portal', roles: ['CLIENTE'], icono: IconoInicio, seccion: 'Principal' },
  { a: '/portal/mascotas', texto: 'Mis mascotas', roles: ['CLIENTE'], icono: IconoHuella, seccion: 'Principal' },
  { a: '/portal/citas', texto: 'Mis citas', roles: ['CLIENTE'], icono: IconoCalendario, seccion: 'Principal' },
  { a: '/portal/pedidos', texto: 'Mis pedidos', roles: ['CLIENTE'], icono: IconoBolsa, seccion: 'Principal' },
  { a: '/catalogo', texto: 'Tienda', roles: ['CLIENTE'], icono: IconoCarrito, seccion: 'Principal' },
  { a: '/panel', texto: 'Dashboard', roles: ['VETERINARIO', 'ADMINISTRADOR'], icono: IconoTablero, seccion: 'General' },
  { a: '/panel/citas', texto: 'Agenda de citas', roles: ['VETERINARIO', 'ADMINISTRADOR'], icono: IconoCalendario, seccion: 'Atención' },
  { a: '/panel/clientes', texto: 'Clientes', roles: ['VETERINARIO', 'ADMINISTRADOR'], icono: IconoUsuarios, seccion: 'Atención' },
  { a: '/panel/mascotas', texto: 'Mascotas', roles: ['VETERINARIO', 'ADMINISTRADOR'], icono: IconoHuella, seccion: 'Atención' },
  { a: '/panel/servicios', texto: 'Servicios', roles: ['ADMINISTRADOR'], icono: IconoPortapapeles, seccion: 'Atención' },
  { a: '/panel/inventario', texto: 'Inventario', roles: ['VETERINARIO', 'ADMINISTRADOR'], icono: IconoCaja, seccion: 'Comercial' },
  { a: '/panel/productos', texto: 'Productos', roles: ['ADMINISTRADOR'], icono: IconoEtiqueta, seccion: 'Comercial' },
  { a: '/panel/pedidos', texto: 'Pedidos', roles: ['ADMINISTRADOR'], icono: IconoBolsa, seccion: 'Comercial' },
  { a: '/catalogo', texto: 'Ver tienda', roles: ['ADMINISTRADOR'], icono: IconoCarrito, seccion: 'Comercial' },
  { a: '/panel/usuarios', texto: 'Usuarios y roles', roles: ['ADMINISTRADOR'], icono: IconoGrafica, seccion: 'Administración' },
  { a: '/perfil', texto: 'Mi perfil', roles: ['CLIENTE', 'VETERINARIO', 'ADMINISTRADOR'], icono: IconoUsuario, seccion: 'Cuenta' },
];

function Marca({ claro = true }: { claro?: boolean }) {
  return (
    <Link to="/" className={`marca ${claro ? '' : 'marca-oscura'}`}>
      <img src={logo} alt="" width={36} height={36} />
      <span>
        Jovan <b>Pets</b>
      </span>
    </Link>
  );
}

export function Avatar({ nombre, tam = 38 }: { nombre: string; tam?: number }) {
  const iniciales = nombre
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join('');
  return (
    <span className="avatar" style={{ width: tam, height: tam, fontSize: tam * 0.38 }} aria-hidden="true">
      {iniciales}
    </span>
  );
}

function BotonCarrito() {
  const { cantidadTotal } = useCarrito();
  const { usuario } = useAuth();
  if (usuario && usuario.rol === 'VETERINARIO') return null;
  return (
    <Link to="/carrito" className="boton-icono" aria-label={`Carrito con ${cantidadTotal} productos`} title="Carrito">
      <IconoCarrito />
      {cantidadTotal > 0 && <span className="punto-contador">{cantidadTotal}</span>}
    </Link>
  );
}

/** Notificaciones: citas pendientes de confirmar (personal) o proximas citas (cliente). */
function Notificaciones() {
  const { usuario } = useAuth();
  const [cantidad, setCantidad] = useState(0);
  useEffect(() => {
    if (!usuario) return;
    const consulta = usuario.rol === 'CLIENTE' ? citasService.listar() : citasService.listar({ estado: 'PENDIENTE' });
    consulta
      .then((c) => setCantidad(usuario.rol === 'CLIENTE' ? c.filter((x) => x.estado === 'CONFIRMADA' || x.estado === 'PENDIENTE').length : c.length))
      .catch(() => setCantidad(0));
  }, [usuario]);
  const destino = usuario?.rol === 'CLIENTE' ? '/portal/citas' : '/panel/citas';
  const texto = usuario?.rol === 'CLIENTE' ? 'citas próximas' : 'citas por confirmar';
  return (
    <Link to={destino} className="boton-icono" title={`${cantidad} ${texto}`} aria-label={`${cantidad} ${texto}`}>
      <IconoCampana />
      {cantidad > 0 && <span className="punto-contador rojo">{cantidad}</span>}
    </Link>
  );
}

/** Sitio publico: inicio, catalogo, acceso y registro. */
export function LayoutPublico() {
  const { usuario } = useAuth();
  const [abierto, setAbierto] = useState(false);
  return (
    <div className="sitio">
      <header className="barra-publica">
        <Marca claro={false} />
        <button type="button" className="menu-movil" aria-label="Abrir menú" onClick={() => setAbierto(!abierto)}>
          ☰
        </button>
        <nav className={`nav-publica ${abierto ? 'abierta' : ''}`} onClick={() => setAbierto(false)}>
          <NavLink to="/" end>
            Inicio
          </NavLink>
          <a href="/#servicios">Servicios</a>
          <NavLink to="/catalogo">Tienda</NavLink>
          {usuario ? (
            <Link className="boton" to={inicioPorRol(usuario.rol)}>
              {usuario.rol === 'CLIENTE' ? 'Mi portal' : 'Ir al panel'}
            </Link>
          ) : (
            <>
              <NavLink to="/login">Iniciar sesión</NavLink>
              <Link to="/registro" className="boton">
                Crear cuenta
              </Link>
            </>
          )}
          <BotonCarrito />
        </nav>
      </header>
      <main className="contenido-publico">
        <Outlet />
      </main>
      <footer className="pie">
        <div className="pie-contenido">
          <Marca claro={false} />
          <p>Veterinaria, peluquería y tienda para mascotas. Agenda citas, consulta el historial y compra en línea.</p>
          <nav>
            <Link to="/catalogo">Tienda</Link>
            <Link to="/login">Iniciar sesión</Link>
            <Link to="/registro">Crear cuenta</Link>
          </nav>
        </div>
      </footer>
    </div>
  );
}

/**
 * Area autenticada. El portal del cliente y el panel administrativo comparten estructura pero
 * se diferencian por color, menu y funciones disponibles segun el rol.
 */
export function LayoutAplicacion() {
  const { usuario, cerrarSesion } = useAuth();
  const navegar = useNavigate();
  const [abierto, setAbierto] = useState(false);
  if (!usuario) return null;
  const esPersonal = usuario.rol !== 'CLIENTE';
  const enlaces = MENU.filter((e) => e.roles.includes(usuario.rol));
  const secciones = [...new Set(enlaces.map((e) => e.seccion))];

  return (
    <div className={`aplicacion ${esPersonal ? 'tema-panel' : 'tema-cliente'}`}>
      <aside className={`lateral ${abierto ? 'abierto' : ''}`}>
        <div className="lateral-marca">
          <Marca />
          <span className="lateral-area">{esPersonal ? 'Panel administrativo' : 'Portal del cliente'}</span>
        </div>
        <nav aria-label="Menú principal" onClick={() => setAbierto(false)}>
          {secciones.map((s) => (
            <div key={s} className="lateral-seccion">
              <span className="lateral-titulo">{s}</span>
              {enlaces
                .filter((e) => e.seccion === s)
                .map((e) => (
                  <NavLink key={e.a + e.texto} to={e.a} end={e.a === '/panel' || e.a === '/portal'}>
                    <e.icono tam={19} />
                    {e.texto}
                  </NavLink>
                ))}
            </div>
          ))}
        </nav>
        <div className="lateral-usuario">
          <Avatar nombre={usuario.nombre} />
          <div>
            <strong>{usuario.nombre}</strong>
            <small>{etiqueta(usuario.rol)}</small>
          </div>
        </div>
      </aside>
      {abierto && <div className="lateral-fondo" onClick={() => setAbierto(false)} />}
      <div className="cuerpo-aplicacion">
        <header className="barra-superior">
          <button type="button" className="menu-movil" aria-label="Abrir menú" onClick={() => setAbierto(!abierto)}>
            ☰
          </button>
          <span className="saludo-barra">
            {esPersonal ? 'Panel administrativo' : 'Portal del cliente'} · <b>{etiqueta(usuario.rol)}</b>
          </span>
          <div className="usuario-barra">
            <Notificaciones />
            <BotonCarrito />
            <Link to="/perfil" className="perfil-barra" title="Mi perfil">
              <Avatar nombre={usuario.nombre} tam={34} />
              <span className="usuario-nombre">{usuario.nombre.split(' ')[0]}</span>
            </Link>
            <button
              type="button"
              className="boton-icono"
              title="Cerrar sesión"
              aria-label="Cerrar sesión"
              onClick={async () => {
                await cerrarSesion();
                navegar('/login');
              }}
            >
              <IconoSalir />
            </button>
          </div>
        </header>
        <main className="contenido">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

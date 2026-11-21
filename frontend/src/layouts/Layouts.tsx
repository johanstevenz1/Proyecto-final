import { useState } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import logo from '../assets/logo.svg';
import { inicioPorRol, useAuth } from '../contexts/AuthContext';
import { useCarrito } from '../contexts/CarritoContext';
import type { Rol } from '../interfaces/tipos';
import { etiqueta } from '../utils/formato';

interface Enlace {
  a: string;
  texto: string;
  roles: Rol[];
}

// Menu del panel segun la matriz de permisos por rol (REQUERIMIENTOS 4.3).
const MENU: Enlace[] = [
  { a: '/portal', texto: 'Mi portal', roles: ['CLIENTE'] },
  { a: '/portal/mascotas', texto: 'Mis mascotas', roles: ['CLIENTE'] },
  { a: '/portal/citas', texto: 'Mis citas', roles: ['CLIENTE'] },
  { a: '/portal/pedidos', texto: 'Mis pedidos', roles: ['CLIENTE'] },
  { a: '/panel', texto: 'Dashboard', roles: ['VETERINARIO', 'ADMINISTRADOR'] },
  { a: '/panel/citas', texto: 'Agenda de citas', roles: ['VETERINARIO', 'ADMINISTRADOR'] },
  { a: '/panel/clientes', texto: 'Clientes', roles: ['VETERINARIO', 'ADMINISTRADOR'] },
  { a: '/panel/mascotas', texto: 'Mascotas', roles: ['VETERINARIO', 'ADMINISTRADOR'] },
  { a: '/panel/inventario', texto: 'Inventario', roles: ['VETERINARIO', 'ADMINISTRADOR'] },
  { a: '/panel/productos', texto: 'Productos', roles: ['ADMINISTRADOR'] },
  { a: '/panel/servicios', texto: 'Servicios', roles: ['ADMINISTRADOR'] },
  { a: '/panel/pedidos', texto: 'Pedidos', roles: ['ADMINISTRADOR'] },
  { a: '/panel/usuarios', texto: 'Usuarios y roles', roles: ['ADMINISTRADOR'] },
  { a: '/catalogo', texto: 'Catálogo', roles: ['CLIENTE', 'ADMINISTRADOR'] },
  { a: '/perfil', texto: 'Mi perfil', roles: ['CLIENTE', 'VETERINARIO', 'ADMINISTRADOR'] },
];

function Marca() {
  return (
    <Link to="/" className="marca">
      <img src={logo} alt="" width={34} height={34} />
      <span>Jovan Pets</span>
    </Link>
  );
}

function BotonCarrito() {
  const { cantidadTotal } = useCarrito();
  const { usuario } = useAuth();
  if (usuario && usuario.rol === 'VETERINARIO') return null;
  return (
    <Link to="/carrito" className="boton-carrito" aria-label={`Carrito con ${cantidadTotal} productos`}>
      🛒 <span className="contador">{cantidadTotal}</span>
    </Link>
  );
}

/** Sitio publico: inicio, catalogo, acceso y registro. */
export function LayoutPublico() {
  const { usuario } = useAuth();
  const [abierto, setAbierto] = useState(false);
  return (
    <div className="sitio">
      <header className="barra-superior">
        <Marca />
        <button type="button" className="menu-movil" aria-label="Abrir menu" onClick={() => setAbierto(!abierto)}>
          ☰
        </button>
        <nav className={`nav-publica ${abierto ? 'abierta' : ''}`} onClick={() => setAbierto(false)}>
          <NavLink to="/" end>
            Inicio
          </NavLink>
          <NavLink to="/catalogo">Catálogo</NavLink>
          {usuario ? (
            <NavLink to={inicioPorRol(usuario.rol)}>{usuario.rol === 'CLIENTE' ? 'Mi portal' : 'Panel'}</NavLink>
          ) : (
            <>
              <NavLink to="/login">Iniciar sesión</NavLink>
              <NavLink to="/registro" className="boton boton-claro">
                Registrarme
              </NavLink>
            </>
          )}
          <BotonCarrito />
        </nav>
      </header>
      <main className="contenido-publico">
        <Outlet />
      </main>
      <footer className="pie">
        <p>Jovan Pets · Veterinaria, peluquería y tienda para mascotas</p>
      </footer>
    </div>
  );
}

/** Area autenticada con menu lateral por rol; el panel administrativo se diferencia del cliente. */
export function LayoutAplicacion() {
  const { usuario, cerrarSesion } = useAuth();
  const navegar = useNavigate();
  const [abierto, setAbierto] = useState(false);
  if (!usuario) return null;
  const enlaces = MENU.filter((e) => e.roles.includes(usuario.rol));
  const esPersonal = usuario.rol !== 'CLIENTE';

  return (
    <div className={`aplicacion ${esPersonal ? 'tema-panel' : 'tema-cliente'}`}>
      <header className="barra-superior">
        <button type="button" className="menu-movil" aria-label="Abrir menu" onClick={() => setAbierto(!abierto)}>
          ☰
        </button>
        <Marca />
        <span className="etiqueta-area">{esPersonal ? 'Panel administrativo' : 'Portal del cliente'}</span>
        <div className="usuario-barra">
          <BotonCarrito />
          <span className="usuario-nombre">
            {usuario.nombre}
            <small>{etiqueta(usuario.rol)}</small>
          </span>
          <button
            type="button"
            className="boton boton-claro"
            onClick={async () => {
              await cerrarSesion();
              navegar('/login');
            }}
          >
            Cerrar sesión
          </button>
        </div>
      </header>
      <div className="cuerpo-aplicacion">
        <aside className={`lateral ${abierto ? 'abierto' : ''}`} onClick={() => setAbierto(false)}>
          <nav aria-label="Menu principal">
            {enlaces.map((e) => (
              <NavLink key={e.a} to={e.a} end={e.a === '/panel' || e.a === '/portal'}>
                {e.texto}
              </NavLink>
            ))}
          </nav>
        </aside>
        <main className="contenido">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Alerta, Campo } from '../components/Comunes';
import { IconoCalendario, IconoEscudo, IconoHuella } from '../components/Iconos';
import { inicioPorRol, useAuth } from '../contexts/AuthContext';
import { mensajeDeError } from '../hooks/useCarga';

/** HU-19: inicio de sesion con redireccion al dashboard segun el rol. */
export function Login() {
  const { iniciarSesion, avisoSesion, limpiarAviso } = useAuth();
  const navegar = useNavigate();
  const ubicacion = useLocation();
  const [email, setEmail] = useState('');
  const [clave, setClave] = useState('');
  const [recordar, setRecordar] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function entrar(correo: string, contrasena: string) {
    setError(null);
    setEnviando(true);
    try {
      const u = await iniciarSesion(correo, contrasena, recordar);
      const desde = (ubicacion.state as { desde?: string } | null)?.desde;
      navegar(desde && desde !== '/login' ? desde : inicioPorRol(u.rol), { replace: true });
    } catch (err) {
      setError(mensajeDeError(err));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="acceso-dividido">
      <aside className="acceso-marca">
        <span className="mascota-grande">🐶🐱</span>
        <h1>Bienvenido a Jovan Pets</h1>
        <p>Ingresa para gestionar la salud y los servicios de tus mascotas.</p>
        <ul>
          <li>
            <IconoCalendario /> Agenda citas veterinarias y de peluquería
          </li>
          <li>
            <IconoHuella /> Consulta el historial y las próximas vacunas
          </li>
          <li>
            <IconoEscudo /> Acceso protegido según tu rol
          </li>
        </ul>
      </aside>
      <section className="acceso-formulario">
        <h2>Iniciar sesión</h2>
        <p className="ayuda">Usa tu correo y contraseña.</p>
        {avisoSesion && (
          <Alerta tipo="aviso" onCerrar={limpiarAviso}>
            {avisoSesion}
          </Alerta>
        )}
        {error && <Alerta tipo="error">{error}</Alerta>}
        <form
          onSubmit={(e: FormEvent) => {
            e.preventDefault();
            void entrar(email, clave);
          }}
        >
          <Campo etiqueta="Correo electrónico" requerido>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" placeholder="tu@correo.com" />
          </Campo>
          <Campo etiqueta="Contraseña" requerido>
            <input type="password" value={clave} onChange={(e) => setClave(e.target.value)} required autoComplete="current-password" />
          </Campo>
          <div className="fila-entre">
            <label className="casilla">
              <input type="checkbox" checked={recordar} onChange={(e) => setRecordar(e.target.checked)} /> Recordarme
            </label>
            <Link to="/recuperar">¿Olvidaste tu contraseña?</Link>
          </div>
          <button className="boton boton-grande boton-bloque" type="submit" disabled={enviando}>
            {enviando ? 'Ingresando...' : 'Ingresar'}
          </button>
        </form>

        <p className="centrado">
          ¿No tienes cuenta? <Link to="/registro">Regístrate gratis</Link>
        </p>
      </section>
    </div>
  );
}

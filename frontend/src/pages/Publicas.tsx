import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Alerta, Campo, Tarjeta } from '../components/Comunes';
import { inicioPorRol, useAuth } from '../contexts/AuthContext';
import { mensajeDeError } from '../hooks/useCarga';
import type { RegistroDatos } from '../interfaces/tipos';
import { ApiError } from '../services/api';
import { authService } from '../services/servicios';

/** HU-18: registro con rol Cliente por defecto. */
export function Registro() {
  const { registrar } = useAuth();
  const navegar = useNavigate();
  const [d, setD] = useState<RegistroDatos & { confirmar: string }>({
    nombre: '', cedula: '', telefono: '', email: '', direccion: '', clave: '', confirmar: '',
  });
  const [errores, setErrores] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const cambiar = (k: keyof typeof d) => (e: { target: { value: string } }) => setD({ ...d, [k]: e.target.value });

  function validar(): Record<string, string> {
    const v: Record<string, string> = {};
    if (!d.nombre.trim()) v.nombre = 'El nombre es obligatorio';
    if (!/^\d{5,15}$/.test(d.cedula.trim())) v.cedula = 'La cédula debe tener entre 5 y 15 dígitos';
    if (!/^[0-9+ ]{7,20}$/.test(d.telefono.trim())) v.telefono = 'Teléfono no válido';
    if (!/^\S+@\S+\.\S+$/.test(d.email.trim())) v.email = 'Email con formato no válido';
    if (!d.direccion.trim()) v.direccion = 'La dirección es obligatoria';
    if (d.clave.length < 8) v.clave = 'La contraseña debe tener mínimo 8 caracteres';
    if (d.clave !== d.confirmar) v.confirmar = 'Las contraseñas no coinciden';
    return v;
  }

  async function enviar(e: FormEvent) {
    e.preventDefault();
    const v = validar();
    setErrores(v);
    setError(null);
    if (Object.keys(v).length) return;
    setEnviando(true);
    try {
      const { confirmar: _confirmar, ...datos } = d;
      void _confirmar;
      await registrar(datos);
      navegar('/portal', { replace: true, state: { bienvenida: true } });
    } catch (err) {
      setError(mensajeDeError(err));
      if (err instanceof ApiError) setErrores(err.errores);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="acceso acceso-grande">
      <Tarjeta titulo="Crear cuenta de cliente">
        {error && <Alerta tipo="error">{error}</Alerta>}
        <form onSubmit={enviar} className="formulario" noValidate>
          <div className="completo">
            <Campo etiqueta="Nombre completo" requerido error={errores.nombre}>
              <input value={d.nombre} onChange={cambiar('nombre')} maxLength={120} autoComplete="name" />
            </Campo>
          </div>
          <Campo etiqueta="Cédula" requerido error={errores.cedula}>
            <input value={d.cedula} onChange={cambiar('cedula')} inputMode="numeric" maxLength={15} />
          </Campo>
          <Campo etiqueta="Teléfono" requerido error={errores.telefono}>
            <input value={d.telefono} onChange={cambiar('telefono')} inputMode="tel" maxLength={20} autoComplete="tel" />
          </Campo>
          <Campo etiqueta="Correo electrónico" requerido error={errores.email}>
            <input type="email" value={d.email} onChange={cambiar('email')} autoComplete="email" />
          </Campo>
          <Campo etiqueta="Dirección" requerido error={errores.direccion}>
            <input value={d.direccion} onChange={cambiar('direccion')} maxLength={200} autoComplete="street-address" />
          </Campo>
          <Campo etiqueta="Contraseña" requerido error={errores.clave} ayuda="Mínimo 8 caracteres">
            <input type="password" value={d.clave} onChange={cambiar('clave')} autoComplete="new-password" />
          </Campo>
          <Campo etiqueta="Confirmar contraseña" requerido error={errores.confirmar}>
            <input type="password" value={d.confirmar} onChange={cambiar('confirmar')} autoComplete="new-password" />
          </Campo>
          <div className="acciones-formulario">
            <Link className="boton boton-secundario" to="/login">
              Ya tengo cuenta
            </Link>
            <button className="boton" type="submit" disabled={enviando}>
              {enviando ? 'Registrando...' : 'Registrarme'}
            </button>
          </div>
        </form>
      </Tarjeta>
    </div>
  );
}

/** HU-22: solicitud del enlace temporal de recuperacion. */
export function RecuperarClave() {
  const [email, setEmail] = useState('');
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  return (
    <div className="acceso">
      <Tarjeta titulo="Recuperar contraseña">
        {mensaje && <Alerta tipo="exito">{mensaje}</Alerta>}
        {error && <Alerta tipo="error">{error}</Alerta>}
        <p>Escribe el correo con el que te registraste. Te enviaremos un enlace válido por 24 horas.</p>
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            setEnviando(true);
            setError(null);
            try {
              setMensaje((await authService.recuperar(email)).mensaje);
            } catch (err) {
              setError(mensajeDeError(err));
            } finally {
              setEnviando(false);
            }
          }}
        >
          <Campo etiqueta="Correo electrónico" requerido>
            <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          </Campo>
          <div className="acciones-formulario">
            <Link className="boton boton-secundario" to="/login">
              Volver
            </Link>
            <button className="boton" type="submit" disabled={enviando}>
              Enviar enlace
            </button>
          </div>
        </form>
      </Tarjeta>
    </div>
  );
}

/** HU-22: nueva contrasena a partir del enlace temporal. */
export function RestablecerClave() {
  const [parametros] = useSearchParams();
  const token = parametros.get('token') ?? '';
  const [clave, setClave] = useState('');
  const [confirmar, setConfirmar] = useState('');
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  return (
    <div className="acceso">
      <Tarjeta titulo="Nueva contraseña">
        {!token && <Alerta tipo="error">El enlace no es válido. Solicita uno nuevo.</Alerta>}
        {mensaje && (
          <Alerta tipo="exito">
            {mensaje} <Link to="/login">Iniciar sesión</Link>
          </Alerta>
        )}
        {error && <Alerta tipo="error">{error}</Alerta>}
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            setError(null);
            if (clave.length < 8) return setError('La contraseña debe tener mínimo 8 caracteres');
            if (clave !== confirmar) return setError('Las contraseñas no coinciden');
            try {
              setMensaje((await authService.restablecer(token, clave)).mensaje);
            } catch (err) {
              setError(mensajeDeError(err));
            }
          }}
        >
          <Campo etiqueta="Nueva contraseña" requerido ayuda="Mínimo 8 caracteres">
            <input type="password" value={clave} onChange={(e) => setClave(e.target.value)} autoComplete="new-password" />
          </Campo>
          <Campo etiqueta="Confirmar contraseña" requerido>
            <input type="password" value={confirmar} onChange={(e) => setConfirmar(e.target.value)} autoComplete="new-password" />
          </Campo>
          <div className="acciones-formulario">
            <button className="boton" type="submit" disabled={!token || !!mensaje}>
              Guardar contraseña
            </button>
          </div>
        </form>
      </Tarjeta>
    </div>
  );
}

/** HU-21: mensaje cuando el rol no permite acceder a una funcionalidad. */
export function AccesoDenegado() {
  const { usuario } = useAuth();
  return (
    <div className="acceso">
      <Tarjeta titulo="Acceso denegado">
        <Alerta tipo="error">No tienes permisos para acceder a esta funcionalidad.</Alerta>
        <Link className="boton" to={usuario ? inicioPorRol(usuario.rol) : '/'}>
          Volver al inicio
        </Link>
      </Tarjeta>
    </div>
  );
}

export function NoEncontrado() {
  return (
    <div className="acceso">
      <Tarjeta titulo="Página no encontrada">
        <p>La página que buscas no existe.</p>
        <Link className="boton" to="/">
          Ir al inicio
        </Link>
      </Tarjeta>
    </div>
  );
}

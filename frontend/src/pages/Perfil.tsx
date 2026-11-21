import { useEffect, useState } from 'react';
import { Alerta, Campo, Encabezado, Tarjeta } from '../components/Comunes';
import { useAuth } from '../contexts/AuthContext';
import { mensajeDeError } from '../hooks/useCarga';
import { sesionActual } from '../services/api';
import { authService, clientesService } from '../services/servicios';
import { etiqueta } from '../utils/formato';

/** Consulta y actualizacion de la informacion personal y cambio de contrasena (RU-05). */
export function Perfil() {
  const { usuario, actualizarSesion } = useAuth();
  const esCliente = usuario?.rol === 'CLIENTE';
  const [datos, setDatos] = useState({ nombre: '', telefono: '', direccion: '', cedula: '', email: '' });
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [claves, setClaves] = useState({ actual: '', nueva: '', confirmar: '' });
  const [mensajeClave, setMensajeClave] = useState<string | null>(null);
  const [errorClave, setErrorClave] = useState<string | null>(null);

  useEffect(() => {
    if (esCliente) {
      clientesService
        .perfil()
        .then((p) => setDatos({ nombre: p.cliente.nombre, telefono: p.cliente.telefono, direccion: p.cliente.direccion, cedula: p.cliente.cedula, email: p.cliente.email }))
        .catch((e) => setError(mensajeDeError(e)));
    }
  }, [esCliente]);

  if (!usuario) return null;
  return (
    <>
      <Encabezado titulo="Mi perfil" migas={[{ texto: esCliente ? 'Portal' : 'Panel', a: esCliente ? '/portal' : '/panel' }]} />
      <div className="rejilla rejilla-2">
        <Tarjeta titulo="Información personal">
          {mensaje && <Alerta tipo="exito">{mensaje}</Alerta>}
          {error && <Alerta tipo="error">{error}</Alerta>}
          {esCliente ? (
            <form
              className="formulario"
              onSubmit={async (e) => {
                e.preventDefault();
                setMensaje(null);
                setError(null);
                try {
                  await clientesService.actualizarPerfil({ nombre: datos.nombre, telefono: datos.telefono, direccion: datos.direccion });
                  const yo = await authService.yo();
                  const sesion = sesionActual();
                  if (sesion) actualizarSesion({ ...sesion, usuario: yo });
                  setMensaje('Datos actualizados correctamente.');
                } catch (err) {
                  setError(mensajeDeError(err));
                }
              }}
            >
              <div className="completo">
                <Campo etiqueta="Nombre completo" requerido>
                  <input value={datos.nombre} maxLength={120} onChange={(e) => setDatos({ ...datos, nombre: e.target.value })} />
                </Campo>
              </div>
              <Campo etiqueta="Cédula">
                <input value={datos.cedula} disabled />
              </Campo>
              <Campo etiqueta="Correo">
                <input value={datos.email} disabled />
              </Campo>
              <Campo etiqueta="Teléfono" requerido>
                <input value={datos.telefono} maxLength={20} onChange={(e) => setDatos({ ...datos, telefono: e.target.value })} />
              </Campo>
              <Campo etiqueta="Dirección" requerido>
                <input value={datos.direccion} maxLength={200} onChange={(e) => setDatos({ ...datos, direccion: e.target.value })} />
              </Campo>
              <div className="acciones-formulario">
                <button className="boton" type="submit">
                  Guardar cambios
                </button>
              </div>
            </form>
          ) : (
            <p>
              <strong>{usuario.nombre}</strong>
              <br />
              {usuario.email}
              <br />
              Rol: {etiqueta(usuario.rol)}
            </p>
          )}
        </Tarjeta>
        <Tarjeta titulo="Cambiar contraseña">
          {mensajeClave && <Alerta tipo="exito">{mensajeClave}</Alerta>}
          {errorClave && <Alerta tipo="error">{errorClave}</Alerta>}
          <form
            onSubmit={async (e) => {
              e.preventDefault();
              setMensajeClave(null);
              setErrorClave(null);
              if (claves.nueva.length < 8) return setErrorClave('La contraseña nueva debe tener mínimo 8 caracteres');
              if (claves.nueva !== claves.confirmar) return setErrorClave('Las contraseñas no coinciden');
              try {
                actualizarSesion(await authService.cambiarClave(claves.actual, claves.nueva));
                setClaves({ actual: '', nueva: '', confirmar: '' });
                setMensajeClave('Contraseña actualizada. Las demás sesiones abiertas se cerraron.');
              } catch (err) {
                setErrorClave(mensajeDeError(err));
              }
            }}
          >
            <Campo etiqueta="Contraseña actual" requerido>
              <input type="password" value={claves.actual} onChange={(e) => setClaves({ ...claves, actual: e.target.value })} autoComplete="current-password" />
            </Campo>
            <Campo etiqueta="Contraseña nueva" requerido ayuda="Mínimo 8 caracteres">
              <input type="password" value={claves.nueva} onChange={(e) => setClaves({ ...claves, nueva: e.target.value })} autoComplete="new-password" />
            </Campo>
            <Campo etiqueta="Confirmar contraseña nueva" requerido>
              <input type="password" value={claves.confirmar} onChange={(e) => setClaves({ ...claves, confirmar: e.target.value })} autoComplete="new-password" />
            </Campo>
            <div className="acciones-formulario">
              <button className="boton" type="submit">
                Actualizar contraseña
              </button>
            </div>
          </form>
        </Tarjeta>
      </div>
    </>
  );
}

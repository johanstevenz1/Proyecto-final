import { useEffect, useState, type FormEvent } from 'react';
import { mensajeDeError, useCarga } from '../hooks/useCarga';
import type { Cliente, Especie, Mascota, MascotaDatos, Sexo } from '../interfaces/tipos';
import { ApiError } from '../services/api';
import { citasService, clientesService } from '../services/servicios';
import { etiqueta, formatoHora, hoyIso } from '../utils/formato';
import { Alerta, Campo, Cargando, Vacio } from './Comunes';

const ESPECIES: Especie[] = ['PERRO', 'GATO', 'AVE', 'OTRO'];

/** HU-05 / RM-05: registro y edicion de mascotas. El personal elige el propietario con un buscador. */
export function FormularioMascota({
  inicial,
  conPropietario,
  propietarioFijo,
  onGuardar,
  onCancelar,
}: {
  inicial?: Mascota;
  conPropietario: boolean;
  propietarioFijo?: Cliente;
  onGuardar: (d: MascotaDatos) => Promise<void>;
  onCancelar: () => void;
}) {
  const [d, setD] = useState<MascotaDatos>({
    propietarioId: inicial?.propietarioId ?? propietarioFijo?.id ?? '',
    nombre: inicial?.nombre ?? '',
    especie: inicial?.especie ?? 'PERRO',
    raza: inicial?.raza ?? '',
    sexo: inicial?.sexo ?? 'MACHO',
    fechaNacimiento: inicial?.fechaNacimiento ?? '',
    peso: inicial?.peso ?? 0,
  });
  const [errores, setErrores] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function enviar(e: FormEvent) {
    e.preventDefault();
    const v: Record<string, string> = {};
    if (conPropietario && !d.propietarioId) v.propietarioId = 'Selecciona el propietario';
    if (!d.nombre.trim()) v.nombre = 'El nombre es obligatorio';
    if (!d.raza.trim()) v.raza = 'La raza es obligatoria';
    if (!d.fechaNacimiento) v.fechaNacimiento = 'La fecha de nacimiento es obligatoria';
    else if (d.fechaNacimiento > hoyIso()) v.fechaNacimiento = 'La fecha no puede ser futura';
    if (!(d.peso > 0) || d.peso > 200) v.peso = 'Ingresa un peso entre 0,01 y 200 kg';
    setErrores(v);
    if (Object.keys(v).length) return;
    setEnviando(true);
    setError(null);
    try {
      await onGuardar({ ...d, propietarioId: conPropietario ? d.propietarioId : undefined });
    } catch (err) {
      setError(mensajeDeError(err));
      if (err instanceof ApiError) setErrores(err.errores);
      setEnviando(false);
    }
  }

  return (
    <form className="formulario" onSubmit={enviar} noValidate>
      {error && (
        <div className="completo">
          <Alerta tipo="error">{error}</Alerta>
        </div>
      )}
      {conPropietario && (
        <div className="completo">
          {propietarioFijo ? (
            <Campo etiqueta="Propietario">
              <input value={`${propietarioFijo.nombre} · ${propietarioFijo.cedula}`} disabled />
            </Campo>
          ) : (
            <BuscadorCliente valor={d.propietarioId ?? ''} error={errores.propietarioId} onElegir={(id) => setD({ ...d, propietarioId: id })} />
          )}
        </div>
      )}
      <Campo etiqueta="Nombre" requerido error={errores.nombre}>
        <input value={d.nombre} maxLength={60} onChange={(e) => setD({ ...d, nombre: e.target.value })} />
      </Campo>
      <Campo etiqueta="Especie" requerido>
        <select value={d.especie} onChange={(e) => setD({ ...d, especie: e.target.value as Especie })}>
          {ESPECIES.map((x) => (
            <option key={x} value={x}>
              {etiqueta(x)}
            </option>
          ))}
        </select>
      </Campo>
      <Campo etiqueta="Raza" requerido error={errores.raza}>
        <input value={d.raza} maxLength={60} onChange={(e) => setD({ ...d, raza: e.target.value })} />
      </Campo>
      <Campo etiqueta="Sexo" requerido>
        <select value={d.sexo} onChange={(e) => setD({ ...d, sexo: e.target.value as Sexo })}>
          <option value="MACHO">Macho</option>
          <option value="HEMBRA">Hembra</option>
        </select>
      </Campo>
      <Campo etiqueta="Fecha de nacimiento" requerido error={errores.fechaNacimiento}>
        <input type="date" max={hoyIso()} value={d.fechaNacimiento} onChange={(e) => setD({ ...d, fechaNacimiento: e.target.value })} />
      </Campo>
      <Campo etiqueta="Peso actual (kg)" requerido error={errores.peso}>
        <input type="number" min={0.01} max={200} step={0.01} value={d.peso || ''} onChange={(e) => setD({ ...d, peso: Number(e.target.value) })} />
      </Campo>
      <div className="acciones-formulario">
        <button type="button" className="boton boton-secundario" onClick={onCancelar}>
          Cancelar
        </button>
        <button type="submit" className="boton" disabled={enviando}>
          {enviando ? 'Guardando...' : 'Guardar mascota'}
        </button>
      </div>
    </form>
  );
}

/** HU-05 criterio 3: seleccion del propietario desde un buscador de clientes. */
export function BuscadorCliente({ valor, error, onElegir }: { valor: string; error?: string; onElegir: (id: string) => void }) {
  const [texto, setTexto] = useState('');
  const [busqueda, setBusqueda] = useState('');
  useEffect(() => {
    const t = window.setTimeout(() => setBusqueda(texto), 300);
    return () => window.clearTimeout(t);
  }, [texto]);
  const resultado = useCarga(() => clientesService.buscar(busqueda, 0, 20, 'nombre'), [busqueda]);
  return (
    <div className="formulario">
      <Campo etiqueta="Buscar propietario" ayuda="Nombre, cédula o teléfono">
        <input type="search" value={texto} onChange={(e) => setTexto(e.target.value)} />
      </Campo>
      <Campo etiqueta="Propietario" requerido error={error}>
        <select value={valor} onChange={(e) => onElegir(e.target.value)}>
          <option value="">{resultado.cargando ? 'Buscando...' : 'Selecciona un cliente'}</option>
          {resultado.datos?.contenido.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nombre} · {c.cedula}
            </option>
          ))}
        </select>
      </Campo>
    </div>
  );
}

/** HU-09: muestra los horarios disponibles de un servicio para la fecha elegida. */
export function SelectorHorario({ servicioId, fecha, valor, onElegir }: { servicioId: string; fecha: string; valor: string; onElegir: (inicio: string) => void }) {
  const disponibilidad = useCarga(
    () => (servicioId && fecha ? citasService.disponibilidad(servicioId, fecha) : Promise.resolve(null)),
    [servicioId, fecha],
  );
  if (!servicioId || !fecha) return <Vacio>Selecciona el servicio y la fecha para ver los horarios.</Vacio>;
  if (disponibilidad.cargando) return <Cargando texto="Consultando horarios..." />;
  if (disponibilidad.error) return <Alerta tipo="error">{disponibilidad.error}</Alerta>;
  const horarios = disponibilidad.datos?.horarios ?? [];
  if (!horarios.length) return <Alerta tipo="aviso">No hay horarios disponibles en esa fecha. Prueba con otro día.</Alerta>;
  return (
    <div className="horarios" role="radiogroup" aria-label="Horas disponibles">
      {horarios.map((h) => (
        <button
          key={h}
          type="button"
          role="radio"
          aria-checked={valor === h}
          className={`horario ${valor === h ? 'seleccionado' : ''}`}
          onClick={() => onElegir(h)}
        >
          {formatoHora(h)}
        </button>
      ))}
    </div>
  );
}

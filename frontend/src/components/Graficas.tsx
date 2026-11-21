// Graficas SVG ligeras para los dashboards (sin librerias externas).

export interface Dato {
  etiqueta: string;
  valor: number;
  detalle?: string;
}

/** Barras verticales con valores encima y etiquetas debajo. */
export function GraficaBarras({ datos, color = 'var(--azul)', formato = (v: number) => String(v), alto = 190 }: { datos: Dato[]; color?: string; formato?: (v: number) => string; alto?: number }) {
  const max = Math.max(1, ...datos.map((d) => d.valor));
  const ancho = 100 / Math.max(datos.length, 1);
  return (
    <svg className="grafica" viewBox={`0 0 100 ${alto / 3}`} preserveAspectRatio="none" role="img" aria-label="Grafica de barras" style={{ height: alto }}>
      {[0.25, 0.5, 0.75, 1].map((p) => (
        <line key={p} x1="0" x2="100" y1={(alto / 3) * (1 - p) * 0.78 + 6} y2={(alto / 3) * (1 - p) * 0.78 + 6} className="grafica-guia" />
      ))}
      {datos.map((d, i) => {
        const h = (d.valor / max) * (alto / 3) * 0.78;
        const x = i * ancho + ancho * 0.2;
        const y = (alto / 3) * 0.78 + 6 - h;
        return (
          <g key={d.etiqueta + i}>
            <title>{`${d.etiqueta}: ${d.detalle ?? formato(d.valor)}`}</title>
            <rect x={x} y={y} width={ancho * 0.6} height={Math.max(h, 0.4)} rx="1" fill={color} opacity={d.valor ? 1 : 0.25} />
          </g>
        );
      })}
    </svg>
  );
}

/** Etiquetas y valores alineados con GraficaBarras (HTML, para que el texto no se deforme). */
export function EjeBarras({ datos, formato = (v: number) => String(v) }: { datos: Dato[]; formato?: (v: number) => string }) {
  return (
    <div className="grafica-eje" style={{ gridTemplateColumns: `repeat(${datos.length}, 1fr)` }}>
      {datos.map((d, i) => (
        <div key={d.etiqueta + i}>
          <strong>{formato(d.valor)}</strong>
          <span>{d.etiqueta}</span>
        </div>
      ))}
    </div>
  );
}

const COLORES = ['#2196f3', '#43a047', '#fb8c00', '#8e24aa', '#e53935', '#00897b'];

/** Grafica de dona con leyenda. */
export function GraficaDona({ datos }: { datos: Dato[] }) {
  const total = datos.reduce((s, d) => s + d.valor, 0);
  let acumulado = 0;
  const r = 15.915; // circunferencia = 100
  return (
    <div className="dona">
      <svg viewBox="0 0 42 42" role="img" aria-label="Distribucion">
        <circle cx="21" cy="21" r={r} fill="none" stroke="#eceff1" strokeWidth="6" />
        {total > 0 &&
          datos.map((d, i) => {
            const porcentaje = (d.valor / total) * 100;
            const segmento = (
              <circle
                key={d.etiqueta}
                cx="21"
                cy="21"
                r={r}
                fill="none"
                stroke={COLORES[i % COLORES.length]}
                strokeWidth="6"
                strokeDasharray={`${porcentaje} ${100 - porcentaje}`}
                strokeDashoffset={25 - acumulado}
              >
                <title>{`${d.etiqueta}: ${d.valor}`}</title>
              </circle>
            );
            acumulado += porcentaje;
            return segmento;
          })}
        <text x="21" y="20" textAnchor="middle" className="dona-total">
          {total}
        </text>
        <text x="21" y="26" textAnchor="middle" className="dona-texto">
          total
        </text>
      </svg>
      <ul className="leyenda-dona">
        {datos.map((d, i) => (
          <li key={d.etiqueta}>
            <span className="muestra" style={{ background: COLORES[i % COLORES.length] }} />
            {d.etiqueta}
            <strong>{d.valor}</strong>
          </li>
        ))}
      </ul>
    </div>
  );
}

/** Barras horizontales para rankings. */
export function BarrasHorizontales({ datos, color = 'var(--verde)', sufijo = '' }: { datos: Dato[]; color?: string; sufijo?: string }) {
  const max = Math.max(1, ...datos.map((d) => d.valor));
  return (
    <ul className="barras-h">
      {datos.map((d) => (
        <li key={d.etiqueta}>
          <div className="barras-h-texto">
            <span>{d.etiqueta}</span>
            <strong>
              {d.valor}
              {sufijo}
            </strong>
          </div>
          <div className="barras-h-pista">
            <div style={{ width: `${(d.valor / max) * 100}%`, background: color }} />
          </div>
        </li>
      ))}
    </ul>
  );
}

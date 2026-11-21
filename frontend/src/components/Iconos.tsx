import type { ReactNode, SVGProps } from 'react';

// Iconos SVG en linea (sin dependencias externas). Usan currentColor para heredar el color del texto.
type Props = SVGProps<SVGSVGElement> & { tam?: number };

function Base({ tam = 20, children, ...resto }: Props & { children: ReactNode }) {
  return (
    <svg width={tam} height={tam} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" {...resto}>
      {children}
    </svg>
  );
}

export const IconoInicio = (p: Props) => (
  <Base {...p}>
    <path d="M3 11l9-7 9 7" />
    <path d="M5 10v10h14V10" />
    <path d="M10 20v-6h4v6" />
  </Base>
);
export const IconoHuella = (p: Props) => (
  <Base {...p}>
    <circle cx="6" cy="9" r="2" />
    <circle cx="10" cy="5" r="2" />
    <circle cx="14" cy="5" r="2" />
    <circle cx="18" cy="9" r="2" />
    <path d="M12 11c-3 0-6 4-6 7 0 2 2 3 3.5 2.5S11 19 12 19s1 1 2.5 1.5S18 20 18 18c0-3-3-7-6-7z" />
  </Base>
);
export const IconoCalendario = (p: Props) => (
  <Base {...p}>
    <rect x="3" y="5" width="18" height="16" rx="2" />
    <path d="M16 3v4M8 3v4M3 10h18" />
  </Base>
);
export const IconoCarrito = (p: Props) => (
  <Base {...p}>
    <circle cx="9" cy="20" r="1.5" />
    <circle cx="18" cy="20" r="1.5" />
    <path d="M2 3h3l2.5 12h11l2-8H6.5" />
  </Base>
);
export const IconoBolsa = (p: Props) => (
  <Base {...p}>
    <path d="M5 8h14l-1 12H6L5 8z" />
    <path d="M9 8V6a3 3 0 016 0v2" />
  </Base>
);
export const IconoCaja = (p: Props) => (
  <Base {...p}>
    <path d="M3 7l9-4 9 4-9 4-9-4z" />
    <path d="M3 7v10l9 4 9-4V7M12 11v10" />
  </Base>
);
export const IconoUsuarios = (p: Props) => (
  <Base {...p}>
    <circle cx="9" cy="8" r="3.5" />
    <path d="M2 20c0-3.5 3-6 7-6s7 2.5 7 6" />
    <path d="M16 4.5a3.5 3.5 0 010 7M18 14c2.5.6 4 2.8 4 6" />
  </Base>
);
export const IconoUsuario = (p: Props) => (
  <Base {...p}>
    <circle cx="12" cy="8" r="4" />
    <path d="M4 21c0-4 3.6-7 8-7s8 3 8 7" />
  </Base>
);
export const IconoGrafica = (p: Props) => (
  <Base {...p}>
    <path d="M3 3v18h18" />
    <path d="M7 15l4-4 3 3 5-6" />
  </Base>
);
export const IconoTablero = (p: Props) => (
  <Base {...p}>
    <rect x="3" y="3" width="7" height="9" rx="1.5" />
    <rect x="14" y="3" width="7" height="5" rx="1.5" />
    <rect x="14" y="12" width="7" height="9" rx="1.5" />
    <rect x="3" y="16" width="7" height="5" rx="1.5" />
  </Base>
);
export const IconoEstetoscopio = (p: Props) => (
  <Base {...p}>
    <path d="M6 3v6a4 4 0 008 0V3" />
    <path d="M10 13v2a5 5 0 0010 0v-2" />
    <circle cx="20" cy="11" r="2" />
  </Base>
);
export const IconoTijeras = (p: Props) => (
  <Base {...p}>
    <circle cx="6" cy="7" r="3" />
    <circle cx="6" cy="17" r="3" />
    <path d="M8.5 8.5L20 19M8.5 15.5L20 5" />
  </Base>
);
export const IconoJeringa = (p: Props) => (
  <Base {...p}>
    <path d="M18 2l4 4M20 4l-9.5 9.5M15 5l4 4" />
    <path d="M14 6L5 15l-1 5 5-1 9-9" />
    <path d="M8 13l3 3" />
  </Base>
);
export const IconoPortapapeles = (p: Props) => (
  <Base {...p}>
    <rect x="5" y="4" width="14" height="17" rx="2" />
    <path d="M9 4V3h6v1M9 10h6M9 14h6M9 18h3" />
  </Base>
);
export const IconoCampana = (p: Props) => (
  <Base {...p}>
    <path d="M6 16V11a6 6 0 0112 0v5l2 2H4l2-2z" />
    <path d="M10 20a2 2 0 004 0" />
  </Base>
);
export const IconoSalir = (p: Props) => (
  <Base {...p}>
    <path d="M15 4h4v16h-4" />
    <path d="M10 8l-4 4 4 4M6 12h10" />
  </Base>
);
export const IconoEscudo = (p: Props) => (
  <Base {...p}>
    <path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6l8-3z" />
    <path d="M9 12l2 2 4-4" />
  </Base>
);
export const IconoReloj = (p: Props) => (
  <Base {...p}>
    <circle cx="12" cy="12" r="9" />
    <path d="M12 7v5l3 2" />
  </Base>
);
export const IconoCamion = (p: Props) => (
  <Base {...p}>
    <path d="M2 6h11v10H2zM13 10h5l3 3v3h-8" />
    <circle cx="6" cy="18" r="1.8" />
    <circle cx="17" cy="18" r="1.8" />
  </Base>
);
export const IconoAlerta = (p: Props) => (
  <Base {...p}>
    <path d="M12 3l10 18H2L12 3z" />
    <path d="M12 10v4M12 17.5v.5" />
  </Base>
);
export const IconoMas = (p: Props) => (
  <Base {...p}>
    <path d="M12 5v14M5 12h14" />
  </Base>
);
export const IconoFlecha = (p: Props) => (
  <Base {...p}>
    <path d="M5 12h14M13 6l6 6-6 6" />
  </Base>
);
export const IconoEtiqueta = (p: Props) => (
  <Base {...p}>
    <path d="M3 12V4h8l10 10-8 8L3 12z" />
    <circle cx="7.5" cy="8.5" r="1.5" />
  </Base>
);

/** Emoji de la especie para los avatares de las mascotas. */
export function emojiEspecie(especie: string): string {
  return especie === 'PERRO' ? '🐶' : especie === 'GATO' ? '🐱' : especie === 'AVE' ? '🦜' : '🐾';
}

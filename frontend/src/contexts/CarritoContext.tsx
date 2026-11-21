import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { Producto } from '../interfaces/tipos';

export interface ItemCarrito {
  productoId: string;
  nombre: string;
  precio: number;
  stock: number;
  cantidad: number;
}

interface CarritoValor {
  items: ItemCarrito[];
  cantidadTotal: number;
  agregar: (p: Producto, cantidad: number) => void;
  cambiarCantidad: (productoId: string, cantidad: number) => void;
  quitar: (productoId: string) => void;
  vaciar: () => void;
}

const CLAVE = 'jovanpets.carrito';
const CarritoContext = createContext<CarritoValor | null>(null);

function leer(): ItemCarrito[] {
  try {
    return JSON.parse(localStorage.getItem(CLAVE) ?? '[]') as ItemCarrito[];
  } catch {
    return [];
  }
}

/**
 * Estado del carrito en el navegador. Es solo una seleccion provisional: el precio, el stock y el
 * total definitivos los calcula y valida el backend al cotizar y al confirmar el pedido.
 */
export function CarritoProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<ItemCarrito[]>(leer);

  useEffect(() => {
    try {
      localStorage.setItem(CLAVE, JSON.stringify(items));
    } catch {
      // sin almacenamiento disponible
    }
  }, [items]);

  const valor = useMemo<CarritoValor>(
    () => ({
      items,
      cantidadTotal: items.reduce((s, i) => s + i.cantidad, 0),
      agregar: (p, cantidad) =>
        setItems((actual) => {
          const existente = actual.find((i) => i.productoId === p.id);
          const nueva = Math.min((existente?.cantidad ?? 0) + cantidad, p.stock, 100);
          if (existente) return actual.map((i) => (i.productoId === p.id ? { ...i, cantidad: nueva, stock: p.stock, precio: p.precio } : i));
          return [...actual, { productoId: p.id, nombre: p.nombre, precio: p.precio, stock: p.stock, cantidad: nueva }];
        }),
      cambiarCantidad: (productoId, cantidad) =>
        setItems((actual) =>
          actual.map((i) => (i.productoId === productoId ? { ...i, cantidad: Math.max(1, Math.min(cantidad, i.stock, 100)) } : i)),
        ),
      quitar: (productoId) => setItems((actual) => actual.filter((i) => i.productoId !== productoId)),
      vaciar: () => setItems([]),
    }),
    [items],
  );

  return <CarritoContext.Provider value={valor}>{children}</CarritoContext.Provider>;
}

export function useCarrito(): CarritoValor {
  const ctx = useContext(CarritoContext);
  if (!ctx) throw new Error('useCarrito debe usarse dentro de CarritoProvider');
  return ctx;
}

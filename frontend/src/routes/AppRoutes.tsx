import { Navigate, Route, Routes } from 'react-router-dom';
import { RutaProtegida } from '../components/Comunes';
import { LayoutAplicacion, LayoutPublico } from '../layouts/Layouts';
import { AgendaCitas } from '../pages/admin/Agenda';
import { Inventario, PedidosAdmin, ProductosAdmin, ServiciosAdmin } from '../pages/admin/Comercial';
import { Dashboard } from '../pages/admin/Dashboard';
import { ClienteDetallePagina, Clientes, MascotasAdmin, Usuarios } from '../pages/admin/Gestion';
import { PortalCliente } from '../pages/cliente/InicioCliente';
import { MisCitas, MisMascotas, MisPedidos, SolicitarCita } from '../pages/cliente/PortalCliente';
import { FichaMascota } from '../pages/FichaMascota';
import { Perfil } from '../pages/Perfil';
import { Inicio } from '../pages/Inicio';
import { Login } from '../pages/Login';
import { AccesoDenegado, NoEncontrado, RecuperarClave, Registro, RestablecerClave } from '../pages/Publicas';
import { Carrito, Catalogo } from '../pages/Tienda';

const PERSONAL = ['VETERINARIO', 'ADMINISTRADOR'] as const;

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<LayoutPublico />}>
        <Route path="/" element={<Inicio />} />
        <Route path="/catalogo" element={<Catalogo />} />
        <Route path="/carrito" element={<Carrito />} />
        <Route path="/login" element={<Login />} />
        <Route path="/registro" element={<Registro />} />
        <Route path="/recuperar" element={<RecuperarClave />} />
        <Route path="/restablecer" element={<RestablecerClave />} />
        <Route path="/acceso-denegado" element={<AccesoDenegado />} />
      </Route>

      <Route
        element={
          <RutaProtegida>
            <LayoutAplicacion />
          </RutaProtegida>
        }
      >
        <Route path="/perfil" element={<Perfil />} />

        <Route path="/portal" element={<RutaProtegida roles={['CLIENTE']}><PortalCliente /></RutaProtegida>} />
        <Route path="/portal/mascotas" element={<RutaProtegida roles={['CLIENTE']}><MisMascotas /></RutaProtegida>} />
        <Route path="/portal/mascotas/:id" element={<RutaProtegida roles={['CLIENTE']}><FichaMascota /></RutaProtegida>} />
        <Route path="/portal/citas" element={<RutaProtegida roles={['CLIENTE']}><MisCitas /></RutaProtegida>} />
        <Route path="/portal/citas/nueva" element={<RutaProtegida roles={['CLIENTE']}><SolicitarCita /></RutaProtegida>} />
        <Route path="/portal/pedidos" element={<RutaProtegida roles={['CLIENTE']}><MisPedidos /></RutaProtegida>} />

        <Route path="/panel" element={<RutaProtegida roles={[...PERSONAL]}><Dashboard /></RutaProtegida>} />
        <Route path="/panel/citas" element={<RutaProtegida roles={[...PERSONAL]}><AgendaCitas /></RutaProtegida>} />
        <Route path="/panel/clientes" element={<RutaProtegida roles={[...PERSONAL]}><Clientes /></RutaProtegida>} />
        <Route path="/panel/clientes/:id" element={<RutaProtegida roles={[...PERSONAL]}><ClienteDetallePagina /></RutaProtegida>} />
        <Route path="/panel/mascotas" element={<RutaProtegida roles={[...PERSONAL]}><MascotasAdmin /></RutaProtegida>} />
        <Route path="/panel/mascotas/:id" element={<RutaProtegida roles={[...PERSONAL]}><FichaMascota /></RutaProtegida>} />
        <Route path="/panel/inventario" element={<RutaProtegida roles={[...PERSONAL]}><Inventario /></RutaProtegida>} />
        <Route path="/panel/productos" element={<RutaProtegida roles={['ADMINISTRADOR']}><ProductosAdmin /></RutaProtegida>} />
        <Route path="/panel/servicios" element={<RutaProtegida roles={['ADMINISTRADOR']}><ServiciosAdmin /></RutaProtegida>} />
        <Route path="/panel/pedidos" element={<RutaProtegida roles={['ADMINISTRADOR']}><PedidosAdmin /></RutaProtegida>} />
        <Route path="/panel/usuarios" element={<RutaProtegida roles={['ADMINISTRADOR']}><Usuarios /></RutaProtegida>} />
      </Route>

      <Route path="/inicio" element={<Navigate to="/" replace />} />
      <Route element={<LayoutPublico />}>
        <Route path="*" element={<NoEncontrado />} />
      </Route>
    </Routes>
  );
}

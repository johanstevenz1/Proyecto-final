# Justificación de las tres vistas

Los diagramas se crearon con la Open API de **Visual Paradigm 18.1** y se exportaron mediante su motor nativo. El archivo `diagramas/Jovan-Pets.vpp` se guardó y reabrió para comprobar sus tres diagramas. Las imágenes conservan la marca de agua de evaluación. Son modelos editables; no son imágenes insertadas dentro de un VPP.

## Vista de contexto

[Abrir imagen](diagramas/01-contexto.png)

Se usa un diagrama de flujo de datos Yourdon/DeMarco de nivel de contexto, siguiendo el círculo central y las entidades externas de la referencia. Jovan Pets es el único proceso. Los actores humanos son Cliente, Personal veterinario y Administrador. Una misma persona puede cumplir más de un rol; se modelan responsabilidades de interacción.

- Cliente: sus datos, mascotas, citas, catálogo y pedidos; HU-09, HU-16 a HU-19, HU-21 y HU-23.
- Personal veterinario: historial, vacunas, citas e inventario; HU-06 a HU-08, HU-10, HU-11, HU-14 y HU-15.
- Administrador: clientes, mascotas, catálogos, roles y métricas; HU-01 a HU-05, HU-12, HU-13, HU-20, HU-21 y HU-24.
- Servicio de correo: integración externa necesaria para HU-22. Está señalada expresamente como pendiente y no como una capacidad operativa.

Las flechas representan información que entra o sale. No representan llamadas entre componentes. El sistema no incluye una pasarela de pago, proveedor de reparto ni prescripción electrónica.

## Vista funcional

[Abrir imagen](diagramas/02-componentes.png)

Cinco componentes funcionales corresponden a las cinco interfaces de servicio reales del backend:

| Componente | Contrato | Implementación | HU |
|---|---|---|---|
| Usuarios y acceso | IUsuarios | UsuarioServicio y configuración transversal de seguridad | HU-18 a HU-22 |
| Clientes y mascotas | IClientesMascotas | ClienteMascotaServicio | HU-01 a HU-08 |
| Citas y servicios | ICitas | CitaServicio | HU-09 a HU-12, HU-23 |
| Productos, pedidos e inventario | IVentas | VentaServicio | HU-13 a HU-17 |
| Panel administrativo | IPanel | PanelServicio | HU-24 |

Son divisiones de un monolito. La agrupación de catálogo, pedido e inventario refleja su coordinación transaccional en VentaServicio. El correo de HU-22 es alcance previsto, todavía sin implementación. La imagen no contiene conectores ni dependencias entre componentes, respetando la restricción explícita de AGENTS.md. Los nombres de los contratos son texto complementario; no se hacen pasar por conexiones UML implementadas.

## Vista lógica

[Abrir imagen](diagramas/03-clases.png)

Se incluyen diez entidades principales: Usuario, Cliente, Mascota, Atencion, Servicio, Cita, Producto, MovimientoInventario, Pedido y DetallePedido. Sus atributos, tipos y visibilidad pública corresponden a las entidades Java reutilizadas. Las referencias entre entidades se muestran como asociaciones, sin duplicarlas como claves foráneas en los atributos.

- Un Cliente tiene cero o muchas Mascota; cada mascota tiene un propietario.
- Una Mascota puede tener cero o muchas Atencion y Cita; cada entrada pertenece a una mascota.
- Un Servicio puede utilizarse en muchas citas; una Cita solicita un servicio.
- Un Cliente tiene cero o muchos Pedido; cada pedido tiene un cliente.
- Pedido compone uno o muchos DetallePedido. El servicio rechaza pedidos vacíos y JPA conserva su ciclo de vida con cascade y orphanRemoval.
- Cada detalle referencia un Producto; un producto puede aparecer en muchos detalles y movimientos.
- Usuario–Cliente es opcional en ambos extremos: puede existir una ficha sin acceso y una cuenta de personal sin ficha de cliente. El servicio exige ficha para el rol CLIENTE.

No se utiliza herencia para roles. No se crea Categoria como clase porque el incremento la almacena como String. Las existencias y el mínimo son atributos de Producto; el historial se conserva en MovimientoInventario. Atencion agrupa los tipos clínicos mediante TipoAtencion. Agenda es una entidad técnica de coordinación de bloqueos, omitida deliberadamente del modelo de dominio principal.

Las operaciones de negocio están en la capa servicio, por lo que no se añaden operaciones inexistentes a las entidades. Esta vista describe el incremento real; los cambios pendientes de HU-02, HU-06, HU-11 y HU-22 están identificados en la matriz.

## Reproducibilidad

`tools/visual-paradigm/EntregaDiagramas.java` contiene el generador. Se compila contra `openapi.jar` de la instalación de Visual Paradigm. La exportación requiere una instalación y licencia válidas de la aplicación; no se incluyen librerías propietarias. La ejecución utilizada se registra en `diagramas/validacion-modelo.txt`.

Documentación oficial: [modelos mediante Open API](https://circle.visual-paradigm.com/docs/open-api/implementing-plug-in/implementing-a-visual-paradigm-plug-in/) y [ejecución de complementos](https://www.visual-paradigm.com/support/documents/vpuserguide/124/255/84449_runningplug-.html).

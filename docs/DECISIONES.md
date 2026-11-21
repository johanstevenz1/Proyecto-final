# Decisiones, supuestos y puntos por confirmar

## Contradicciones resueltas

- **Base de datos:** `REQUERIMIENTOS.md` proponía PostgreSQL/MySQL y la versión anterior del repositorio usaba JPA y H2. El `AGENTS.md` vigente exige MongoDB, así que se reconstruyó la persistencia con Spring Data MongoDB y se eliminó toda dependencia relacional.
- **Frontend:** la versión anterior tenía HTML y JavaScript sin framework. Se reemplazó por React + TypeScript, como exige `AGENTS.md`, siguiendo los wireframes de `docs/wireframes`.
- **Roles:** las historias definen Cliente, Personal veterinario y Administrador (ROL-01). No se creó un rol de peluquería: el personal gestiona ambas agendas. Si se necesita, basta con agregar el valor al enum `Rol`.
- **Vista lógica:** el diagrama entregado indica identificadores `Long` y “Java/JPA”. En la implementación los `id` son `String` (ObjectId de MongoDB). Se recomienda actualizar esa etiqueta del diagrama.

## Supuestos (confirmar con el negocio)

1. **Jornada de atención:** 08:00–12:00 y 14:00–18:00, todos los días, en bloques de 15 minutos. Es configurable con `app.agenda.jornadas`. No se definieron días festivos ni de cierre.
2. **Una agenda por categoría:** veterinaria y peluquería tienen agendas independientes, con una cita simultánea por agenda. Si hay varios profesionales atendiendo en paralelo, hay que modelar una agenda por profesional.
3. **Profesional de la cita:** se asigna al personal que confirma la cita. El filtro por veterinario de HU-11 usa ese dato.
4. **Medicamentos con fórmula:** los productos marcados con `requiereFormula` se muestran en el catálogo, pero no se venden en línea (AGENTS §4.5).
5. **Stock bajo:** la alerta aparece cuando el stock es **menor** al mínimo (texto literal de HU-15).
6. **Anticipación máxima de las citas:** 90 días.
7. **Pedidos:** sin pasarela de pago (fuera del límite del contexto). Estados: Pendiente → Confirmado → Despachado (“Listo para recoger” si es recogida) → Entregado; se cancela antes del despacho.
8. **Eliminar cliente (HU-04):** es una desactivación lógica que también desactiva su cuenta, para conservar el historial clínico y los pedidos.

## Pendientes

- **Correo (HU-22, HU-10 criterio 4, HU-18 criterio 5):** la vista de contexto marca el servicio de correo como “integración pendiente”. El backend ya genera el enlace temporal y lo entrega a la interfaz `ServicioCorreo`; la implementación actual (`CorreoRegistroLocalService`) lo escribe en el registro del servidor. Falta elegir el proveedor SMTP o API e implementar esa interfaz.
- **Adjuntar archivos al historial (RM-07, prioridad baja):** no está en las historias aprobadas; no se implementó.
- **Recordatorios de citas (RA-05):** depende del servicio de correo.
- **Respaldo automático (RNF-05):** corresponde a la infraestructura de MongoDB (por ejemplo, `mongodump` programado o el respaldo del proveedor). Aún no hay hosting seleccionado.
- **Despliegue final:** no se ha elegido proveedor. La vista de despliegue muestra el entorno local y una topología genérica de tres nodos.

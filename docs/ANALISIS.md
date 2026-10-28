# Análisis de Jovan Pets y decisiones de entrega

Fecha: 27 de octubre de 2026, America/Bogota.

## Evidencia de partida

La rama `main`, en `1d463339519c46c8f4267aee3b5901aab4e22956`, tenía un árbol vacío. Sus dos últimos commits eliminaron los requisitos y los wireframes. Las 24 historias seguían disponibles en las incidencias de GitHub, con actor, objetivo y criterios de aceptación. Se leyeron sus textos y se recuperaron los documentos del commit `01c72eaf12bfff5bb9449f270ee91d20aab1ef35`.

Se encontró además una entrega local anterior del mismo proyecto, del 25 de octubre. Se conservaron su backend Java 21/Spring Boot 3.5.16, frontend HTML/CSS/JavaScript y esquema relacional. Esa entrega es la procedencia del código reutilizado; no se presenta como código encontrado en el `main` actual. Su análisis anterior no había leído las incidencias y atribuía cobertura completa a historias que tienen criterios pendientes. Esta entrega sustituye aquel análisis por la [matriz actualizada](TRAZABILIDAD.md).

## Cambios acotados

- HU-23: el servicio ahora rechaza cancelar una cita cuyo inicio ya pasó. Comprueba esta regla antes de cambiar su estado; el horario se libera únicamente cuando la cancelación es válida.
- HU-13: los productos deben tener precio estrictamente mayor que cero. Se conserva la validación decimal y la actualización controlada de existencias.
- Se añadieron dos pruebas de integración para estos rechazos y para comprobar que la operación fallida no deja datos modificados.
- Se recuperaron los 24 PNG y once HTML originales. El índice abre el PNG cuando no existe el HTML correspondiente.
- Se conservaron los identificadores y textos originales de las 24 HU y se documentó la cobertura parcial sin alterar sus criterios.
- Se generaron tres diagramas nativos nuevos en Visual Paradigm, con encabezados amarillos, componentes color crema y clases verdes, siguiendo las referencias visuales.

## Problemas de coherencia y decisiones

| Hallazgo | Tratamiento |
|---|---|
| HU-22 sí fija expiración de 24 horas; la entrega anterior decía que faltaba definirla. | Se corrige la documentación. La recuperación por correo sigue sin implementar; se representa como integración pendiente. |
| HU-02 pide historial de cambios de cliente. | Se marca parcial: el código actual edita la ficha sin auditoría histórica. |
| HU-06 pide diagnóstico, observaciones, edición y eliminación de antecedentes. | Se marca parcial: la entidad Atencion conserva una descripción general y el incremento permite altas y consultas. |
| HU-11 pide filtro por veterinario; el código agenda por categoría. | Se marca parcial. No se inventa una clase Veterinario ni una asignación inexistente en el diagrama de la implementación. |
| La documentación inicial menciona peluquería y entrega a domicilio; las HU no definen sus reglas específicas. | Se conserva lo ya implementado como decisión de prototipo. Requiere confirmación de negocio; no se describe como requisito aprobado por HU-12 o HU-17. |
| El código permite a personal o propietarios algunas operaciones cuyas HU nombran al administrador. | Se registra la ampliación preexistente de permisos para revisión, sin atribuirla a las HU. |
| La vista de componentes de ejemplo tiene conectores; AGENTS.md prohíbe dependencias. | La revisión del 28 de octubre conecta el portal/API y los módulos mediante puertos e interfaces provistas/requeridas, con líneas continuas. No hay flechas UML Dependency entre componentes. |
| Las entidades JPA no implementan operaciones de negocio. | El diagrama lógico muestra sus atributos y asociaciones. Las operaciones reales residen en servicios; no se inventan métodos para imitar el ejemplo. |

## Reglas de prototipo conservadas

Dos agendas de capacidad uno, VETERINARIA y PELUQUERIA; jornadas de 08:00–12:00 y 14:00–18:00, con inicios cada 15 minutos. Se reserva toda la duración del servicio. Estos horarios, categorías y capacidades requieren ratificación; no existe asignación a múltiples profesionales.

El pedido descuenta existencias al confirmarse en una transacción. El servidor calcula precios y totales, conserva el precio aplicado en cada detalle y bloquea productos para evitar sobreventa concurrente. Cancelar antes del despacho devuelve existencias una sola vez. RECOGIDA y DOMICILIO son modalidades preexistentes del prototipo, sin pagos electrónicos, tarifas de envío, promesas de cobertura o integración de transportistas.

Las cuentas públicas se registran siempre como CLIENTE. Se usa BCrypt, sesión de 30 minutos, CSRF y verificación de rol vigente y propiedad de los datos en el servidor. Las cuentas de demostración son ficticias y están documentadas en el README.

## Trabajo pendiente, en orden

1. Acordar las ampliaciones de permisos y las reglas de agendas, peluquería y entregas.
2. Completar HU-22 con token de uso único de 24 horas y proveedor de correo configurado; HU-10 necesita notificaciones.
3. Completar auditoría de clientes, gestión clínica detallada, paginación, filtro por veterinario y categorías predefinidas del servidor.
4. Validar el perfil PostgreSQL en un servidor real y añadir migraciones y respaldos. H2 se utilizó en las pruebas de esta entrega.
5. Revisar las tres vistas y después desarrollar las vistas gráficas de desarrollo y despliegue indicadas en AGENTS.md. No se realizó un nuevo despliegue público.

La entrega contiene un incremento ejecutable y los tres entregables prioritarios. No declara terminadas las 24 HU ni atribuye resultados de pruebas a flujos que no se ejecutaron.

## Revisión del 28 de octubre de 2026

A solicitud del estudiante, se revisaron las diapositivas de componentes de Modelo de presentación de diseño y EcoGestion_3_diagramas_relaciones_conectadas. La vista funcional incorpora su notación de puertos, interfaz provista circular e interfaz requerida semicircular. Se mantiene el alcance de Jovan Pets y se utilizan los cinco contratos reales de ApiControlador. Las vistas de contexto y lógica se conservan. No se modifica el backend en esta revisión.

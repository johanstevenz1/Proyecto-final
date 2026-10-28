# Historias originales de GitHub

Copia de lectura del 27 de octubre de 2026. Los identificadores, actores y criterios se conservan. Fuente: incidencias #1 a #24 de https://github.com/johanstevenz1/Proyecto-final.

# HU-01: Registrar cliente con datos personales

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/1

## Historia de Usuario HU-01

**Como:** Administrador del sistema
**Quiero:** Poder registrar clientes con sus datos personales
**Para que:** Se mantenga un registro organizado de los propietarios de mascotas

### Criterios de Aceptación

1. El formulario debe solicitar: nombre completo, cédula, teléfono, email y dirección.
2. La cédula debe ser única en el sistema (no duplicados).
3. El email debe tener formato válido.
4. Al guardar, el sistema debe confirmar el registro exitoso.
5. Los campos obligatorios deben estar marcados y validados.

### Datos Obligatorios
- Nombre completo
- Cédula
- Teléfono
- Email
- Dirección

### Visualizacion
<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/a99546b0-4fe3-4524-8933-963bf7784723" />

# HU-02: Editar información de cliente

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/2

## Historia de Usuario HU-02

**Como:** Administrador del sistema
**Quiero:** Editar la información de un cliente existente
**Para que:** Los datos se mantengan actualizados

### Criterios de Aceptación

1. Se debe poder buscar el cliente por cédula o nombre.
2. Todos los campos deben ser editables excepto la cédula.
3. Al guardar los cambios, el sistema debe confirmar la actualización.
4. Se debe mantener un historial de cambios realizados.

### Visualizacion

<img width="1448" height="1086" alt="Image" src="https://github.com/user-attachments/assets/6be5249e-b79b-4c9d-8bd8-b3c83605786f" />

# HU-03: Buscar clientes por nombre, cédula o teléfono

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/3

## Historia de Usuario HU-03

**Como:** Administrador del sistema
**Quiero:** Buscar clientes por nombre, cédula o teléfono
**Para que:** Pueda encontrar rápidamente la información de un cliente

### Criterios de Aceptación

1. El sistema debe permitir búsqueda por nombre (parcial o total).
2. El sistema debe permitir búsqueda por cédula exacta.
3. El sistema debe permitir búsqueda por número de teléfono.
4. Los resultados deben mostrarse en una tabla ordenada.
5. Debe existir un campo de búsqueda accesible desde el listado.

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/7747e0fe-8680-4d3c-9e01-afd10eeb6b6d" />

# HU-04: Visualizar listado de clientes

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/4

## Historia de Usuario HU-04

**Como:** Administrador del sistema
**Quiero:** Visualizar el listado de todos los clientes registrados
**Para que:** Pueda tener una vista general de los clientes del sistema

### Criterios de Aceptación

1. El listado debe mostrar nombre, cédula, teléfono y email.
2. Debe permitir paginación si hay muchos registros.
3. Se debe poder ordenar por nombre o fecha de registro.
4. Cada registro debe tener opciones de editar y eliminar.

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/3d22aed7-bfff-4a39-85e8-752bc4c26213" />

# HU-05: Registrar mascota asociada a un cliente

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/5

## Historia de Usuario HU-05

**Como:** Administrador del sistema
**Quiero:** Registrar una nueva mascota asociada a un cliente
**Para que:** Cada mascota tenga su ficha individual con datos y historial

### Criterios de Aceptación

1. El formulario debe solicitar: nombre, especie, raza, sexo, fecha de nacimiento y peso.
2. La mascota debe estar vinculada a un cliente propietario existente.
3. Se debe poder seleccionar el propietario desde un buscador de clientes.
4. Al guardar, el sistema debe confirmar el registro exitoso.
5. La mascota debe quedar visible en el historial del cliente.

### Datos Obligatorios
- Nombre de la mascota
- Especie (perro, gato, ave, otro)
- Raza
- Sexo
- Fecha de nacimiento
- Peso actual

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/6660cea0-e36a-45e3-96c4-8dbe7f1cff99" />

# HU-06: Registrar antecedentes médicos de mascota

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/6

## Historia de Usuario HU-06

**Como:** Personal veterinario
**Quiero:** Registrar antecedentes médicos de una mascota
**Para que:** Se mantenga un historial completo de su salud

### Criterios de Aceptación

1. Se debe poder agregar una nueva entrada de antecedente a la mascota.
2. Cada entrada debe incluir: fecha, tipo de atención, diagnóstico y observaciones.
3. Los antecedentes deben mostrarse en orden cronológico inverso (más reciente primero).
4. Se debe poder editar o eliminar entradas existentes.

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/3df79fe8-6d28-4822-9c09-cfa87d359337" />

# HU-07: Visualizar historial de controles de mascota

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/7

## Historia de Usuario HU-07

**Como:** Personal veterinario
**Quiero:** Visualizar el historial de controles y atenciones de una mascota
**Para que:** Pueda tomar decisiones informadas sobre su tratamiento

### Criterios de Aceptación

1. El historial debe mostrar fecha, tipo de atención y resumen.
2. Se debe poder filtrar por rango de fechas.
3. Se debe poder expandir cada entrada para ver detalles completos.
4. El historial debe ser accesible desde la ficha de la mascota.

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/39e4b3f5-6b13-476b-9b78-821185578c0f" />

# HU-08: Registrar vacunas y desparasitaciones

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/8

## Historia de Usuario HU-08

**Como:** Personal veterinario
**Quiero:** Registrar vacunas y desparasitaciones de una mascota
**Para que:** Se lleve un control preventivo de su salud

### Criterios de Aceptación

1. Se debe poder registrar vacuna con: nombre, fecha de aplicación y próxima dosis.
2. Se debe poder registrar desparasitación con: producto, fecha y próxima dosis.
3. Las vacunas y desparasitaciones deben mostrarse en secciones separadas.
4. Se debe mostrar la próxima fecha programada para cada vacuna/desparasitación.

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/76548fd6-5fc3-409b-afa5-161541d776df" />

# HU-09: Solicitar cita desde el portal del cliente

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/9

## Historia de Usuario HU-09

**Como:** Cliente
**Quiero:** Solicitar una cita seleccionando fecha, hora y servicio
**Para que:** Pueda agendar una atención para mi mascota de forma sencilla

### Criterios de Aceptación

1. El cliente debe poder seleccionar la mascota para la cita.
2. Debe poder elegir el tipo de servicio (consulta, vacunación, etc.).
3. Debe poder seleccionar fecha y hora disponible.
4. El sistema debe mostrar horarios disponibles en la fecha seleccionada.
5. Al confirmar, el sistema debe mostrar un resumen de la cita agendada.

### Visualizacion

<img width="1402" height="1122" alt="Image" src="https://github.com/user-attachments/assets/60698b62-144a-4dc3-a079-1af285f875a8" />

# HU-10: Confirmar o rechazar citas por parte del personal

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/10

## Historia de Usuario HU-10

**Como:** Personal veterinario
**Quiero:** Confirmar o rechazar citas solicitadas
**Para que:** Pueda gestionar mi agenda de atención eficientemente

### Criterios de Aceptación

1. El personal debe ver las citas pendientes de confirmación.
2. Debe poder confirmar la cita con un click.
3. Debe poder rechazar la cita indicando un motivo.
4. Al confirmar/rechazar, el cliente debe recibir una notificación.

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/97b2bc47-6911-408c-b037-faddbca4fced" />

# HU-11: Visualizar calendario de citas

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/11

## Historia de Usuario HU-11

**Como:** Personal veterinario
**Quiero:** Visualizar el calendario de citas del día y la semana
**Para que:** Pueda planificar mi agenda de atenciones

### Criterios de Aceptación

1. Debe existir una vista de calendario diario y semanal.
2. Cada cita debe mostrar: hora, cliente, mascota y servicio.
3. Debe poder filtrar por veterinario.
4. Las citas confirmadas deben diferenciarse visualmente de las pendientes.

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/bfa08a08-5914-4fae-9e83-e7f4ef624ad5" />

# HU-12: Registrar servicios veterinarios disponibles

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/12

## Historia de Usuario HU-12

**Como:** Administrador del sistema
**Quiero:** Registrar los servicios veterinarios disponibles
**Para que:** Los clientes puedan ver qué servicios ofrece la veterinaria

### Criterios de Aceptación

1. Cada servicio debe tener: nombre, descripción, precio y duración estimada.
2. Los servicios deben ser visibles para los clientes al agendar citas.
3. Se debe poder activar/desactivar un servicio.
4. Debe existir un listado administrable de servicios.

### Datos del Servicio
- Nombre del servicio
- Descripción
- Precio
- Duración estimada (minutos)

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/a9edc2ee-a825-439c-80cf-89dc7cf80b01" />

# HU-13: Registrar producto con datos e inventario

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/13

## Historia de Usuario HU-13

**Como:** Administrador del sistema
**Quiero:** Registrar productos con sus datos e inventario inicial
**Para que:** Se pueda controlar el stock de productos de la veterinaria

### Criterios de Aceptación

1. Cada producto debe tener: nombre, descripción, categoría, precio y stock inicial.
2. Las categorías deben ser predefinidas (medicamentos, alimentos, accesorios, etc.).
3. El precio debe ser mayor a cero.
4. El stock no puede ser negativo.
5. Al guardar, el sistema debe confirmar el registro exitoso.

### Datos del Producto
- Nombre del producto
- Descripción
- Categoría
- Precio unitario
- Stock inicial

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/403f3107-0f74-4e29-8699-07e41aa0da96" />

# HU-14: Registrar entradas y salidas de stock

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/14

## Historia de Usuario HU-14

**Como:** Personal veterinario
**Quiero:** Registrar entradas y salidas de stock de productos
**Para que:** Se mantenga un control preciso del inventario

### Criterios de Aceptación

1. Se debe poder registrar una entrada de stock con: producto, cantidad, motivo y fecha.
2. Se debe poder registrar una salida de stock con: producto, cantidad, motivo y fecha.
3. Las entradas deben incrementar el stock actual.
4. Las salidas deben decrementar el stock actual.
5. No se debe permitir salidas superiores al stock disponible.

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/84046977-fba0-4261-8b56-579de895f6d8" />

# HU-15: Alertas de stock bajo

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/15

## Historia de Usuario HU-15

**Como:** Personal veterinario
**Quiero:** Visualizar alertas cuando el stock de un producto esté bajo
**Para que:** Se puedan reabastecer productos antes de que se agoten

### Criterios de Aceptación

1. Cada producto debe tener un stock mínimo configurado.
2. Cuando el stock actual sea menor al mínimo, debe aparecer una alerta.
3. Las alertas deben ser visibles en el dashboard principal.
4. Debe existir un listado de productos con stock bajo.

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/dc154cb2-9137-4455-b26f-c6b997833956" />

# HU-16: Consultar productos y agregar al carrito

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/16

## Historia de Usuario HU-16

**Como:** Cliente
**Quiero:** Consultar los productos disponibles y agregarlos al carrito
**Para que:** Pueda realizar pedidos de productos para mi mascota

### Criterios de Aceptación

1. El cliente debe poder ver el catálogo de productos con precio y stock.
2. Debe poder buscar productos por nombre o categoría.
3. Debe poder agregar productos al carrito con cantidad.
4. El carrito debe mostrar el total a pagar.
5. No se deben permitir cantidades mayores al stock disponible.

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/f3cf2148-90d4-4315-8858-bd96518e65e0" />

# HU-17: Realizar pedido desde la plataforma

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/17

## Historia de Usuario HU-17

**Como:** Cliente
**Quiero:** Realizar un pedido desde la plataforma
**Para que:** Pueda adquirir productos sin necesidad de ir a la tienda

### Criterios de Aceptación

1. El cliente debe poder confirmar el pedido desde el carrito.
2. El sistema debe generar un número de pedido único.
3. El stock de los productos debe decrementarse al confirmar el pedido.
4. El cliente debe recibir una confirmación con el resumen del pedido.
5. El pedido debe quedar registrado con estado "Pendiente".

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/3fb63d41-4b58-438e-a008-43bc8a316e4c" /> 

# HU-18: Registrar usuario con credenciales

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/18

## Historia de Usuario HU-18

**Como:** Usuario
**Quiero:** Poder registrarme con credenciales (email y contraseña)
**Para que:** Pueda acceder al sistema con mi cuenta personal

### Criterios de Aceptación

1. El email debe ser único en el sistema.
2. La contraseña debe tener mínimo 8 caracteres.
3. La contraseña debe almacenarse con hash y sal.
4. Al registrarse, el usuario debe tener rol de "Cliente" por defecto.
5. Se debe enviar un email de confirmación (opcional).

###  Visualizacion 
<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/e61c8f46-0824-42ca-96c0-e25cd90daf64" />

# HU-19: Iniciar sesión con credenciales

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/19

## Historia de Usuario HU-19

**Como:** Usuario
**Quiero:** Iniciar sesión con mi email y contraseña
**Para que:** Pueda acceder a las funcionalidades según mi rol

### Criterios de Aceptación

1. El sistema debe validar email y contraseña.
2. Si las credenciales son incorrectas, mostrar mensaje de error.
3. Al iniciar sesión, el sistema debe redirigir al dashboard correspondiente al rol.
4. La sesión debe expirar después de un tiempo de inactividad.
5. Debe existir opción "Recordarme" (opcional).

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/895ba786-7ded-40a1-94e5-91d0beaa346c" />

# HU-20: Asignar roles a los usuarios

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/20

## Historia de Usuario HU-20

**Como:** Administrador del sistema
**Quiero:** Asignar roles a los usuarios (Cliente, Personal Veterinario, Administrador)
**Para que:** Cada usuario tenga acceso únicamente a las funcionalidades de su rol

### Criterios de Aceptación

1. El administrador debe poder ver el listado de usuarios con sus roles.
2. Debe poder cambiar el rol de un usuario.
3. Los roles disponibles deben ser: Cliente, Personal Veterinario, Administrador.
4. Al cambiar el rol, el usuario debe tener acceso inmediato a las nuevas funcionalidades.

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/41223f17-8328-4989-a75f-d6a26d96ea54" />

# HU-21: Control de acceso según rol

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/21

## Historia de Usuario HU-21

**Como:** Administrador del sistema
**Quiero:** Controlar el acceso según el rol asignado a cada usuario
**Para que:** Se garantice la seguridad y privacidad de la información

### Criterios de Aceptación

1. Un Cliente solo puede ver sus propias mascotas y citas.
2. Un Cliente no puede acceder a gestión de inventario o usuarios.
3. Un Personal Veterinario puede gestionar citas e inventario.
4. Un Administrador tiene acceso a todas las funcionalidades.
5. Si un usuario intenta acceder a una funcionalidad no autorizada, debe ver un mensaje de "Acceso denegado".

### Visualizacion

<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/e98b631e-9626-4d8c-b72f-12f192ad50e8" />

# HU-22: Recuperar contraseña por email

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/22

## Historia de Usuario HU-22

**Como:** Usuario
**Quiero:** Poder recuperar mi contraseña por email
**Para que:** Pueda volver a acceder al sistema si la olvido

### Criterios de Aceptación

1. Debe existir un enlace "¿Olvidaste tu contraseña?" en la pantalla de login.
2. Al hacer clic, se debe solicitar el email registrado.
3. Se debe enviar un email con un enlace temporal para restablecer la contraseña.
4. El enlace debe expirar después de 24 horas.
5. La nueva contraseña debe cumplir con los requisitos de seguridad.

### Visualizacion
<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/fb0981a2-592e-4544-88fd-50689a5fbfed" />

# HU-23: Cancelar cita con motivo

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/23

## Historia de Usuario HU-23

**Como:** Cliente
**Quiero:** Poder cancelar una cita con un motivo
**Para que:** El personal pueda gestionar la disponibilidad de horarios

### Criterios de Aceptación

1. El cliente solo puede cancelar citas futuras.
2. Debe indicar un motivo de cancelación.
3. Al cancelar, la cita debe cambiar de estado a "Cancelada".
4. El horario cancelado debe quedar disponible para otros clientes.

### Visualizacion
<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/792b4f78-8985-44e5-9dee-1dd96c7901e5" />

# HU-24: Dashboard principal con métricas

Fuente: https://github.com/johanstevenz1/Proyecto-final/issues/24

## Historia de Usuario HU-24

**Como:** Administrador del sistema
**Quiero:** Tener un dashboard principal con métricas y resúmenes
**Para que:** Pueda tener una vista general del estado de la veterinaria

### Criterios de Aceptación

1. Mostrar cantidad de clientes registrados.
2. Mostrar cantidad de mascotas registradas.
3. Mostrar citas del día (pendientes, confirmadas, completadas).
4. Mostrar alertas de stock bajo.
5. Mostrar resumen de pedidos recientes.

### Visualizacion
<img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/8ce71d0f-f164-4395-966b-fe4b044a2c1d" />

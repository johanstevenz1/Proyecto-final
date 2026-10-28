# Jovan Pets – Requerimientos del Sistema

## 1. Gestión de Clientes y Mascotas

### 1.1 Módulo de Clientes

| ID | Requerimiento | Prioridad |
|----|--------------|-----------|
| RC-01 | Registrar cliente con datos personales (nombre, cédula, teléfono, email, dirección) | Alta |
| RC-02 | Editar información del cliente existente | Alta |
| RC-03 | Buscar clientes por nombre, cédula o teléfono | Alta |
| RC-04 | Visualizar listado de clientes registrados | Media |
| RC-05 | Eliminar o desactivar cliente del sistema | Media |
| RC-06 | Asignar múltiples mascotas a un cliente | Alta |

### 1.2 Módulo de Mascotas

| ID | Requerimiento | Prioridad |
|----|--------------|-----------|
| RM-01 | Registrar mascota con datos básicos (nombre, especie, raza, sexo, fecha de nacimiento, peso) | Alta |
| RM-02 | Asociar mascota a un cliente propietario | Alta |
| RM-03 | Registrar antecedentes médicos de la mascota | Alta |
| RM-04 | Visualizar historial de controles y atenciones | Alta |
| RM-05 | Editar información de la mascota | Alta |
| RM-06 | Registrar vacunas y desparasitaciones | Media |
| RM-07 | Adjuntar archivos o imágenes al historial | Baja |

---

## 2. Gestión de Citas

### 2.1 Módulo de Agendamiento

| ID | Requerimiento | Prioridad |
|----|--------------|-----------|
| RA-01 | Solicitar cita desde el portal del cliente seleccionando fecha, hora y servicio | Alta |
| RA-02 | Visualizar disponibilidad de horarios del veterinario | Alta |
| RA-03 | Confirmar o rechazar cita por parte del personal | Alta |
| RA-04 | Cancelar cita con motivo por parte del cliente o personal | Media |
| RA-05 | Recibir notificación de recordatorio antes de la cita | Media |
| RA-06 | Visualizar calendario de citas del día/semana | Alta |
| RA-07 | Reagendar cita existente | Media |

### 2.2 Módulo de Servicios Veterinarios

| ID | Requerimiento | Prioridad |
|----|--------------|-----------|
| RS-01 | Registrar servicios veterinarios disponibles (consulta, vacunación, cirugía, etc.) | Alta |
| RS-02 | Asociar precio y duración estimada a cada servicio | Alta |
| RS-03 | Visualizar catálogo de servicios para el cliente | Media |
| RS-04 | Editar o eliminar servicios del catálogo | Media |

---

## 3. Gestión de Productos e Inventario

### 3.1 Módulo de Productos

| ID | Requerimiento | Prioridad |
|----|--------------|-----------|
| RP-01 | Registrar producto con datos (nombre, descripción, categoría, precio, stock) | Alta |
| RP-02 | Editar información del producto | Alta |
| RP-03 | Visualizar catálogo de productos disponibles | Alta |
| RP-04 | Buscar productos por nombre o categoría | Media |
| RP-05 | Eliminar o desactivar producto del catálogo | Media |

### 3.2 Módulo de Inventario

| ID | Requerimiento | Prioridad |
|----|--------------|-----------|
| RI-01 | Registrar entrada de stock (compra/proveedor) | Alta |
| RI-02 | Registrar salida de stock (venta) | Alta |
| RI-03 | Visualizar stock actual de cada producto | Alta |
| RI-04 | Alertar cuando el stock esté por debajo del mínimo configurado | Media |
| RI-05 | Visualizar historial de movimientos de inventario | Media |

### 3.3 Módulo de Pedidos (Cliente)

| ID | Requerimiento | Prioridad |
|----|--------------|-----------|
| RPE-01 | Consultar productos disponibles desde el portal del cliente | Alta |
| RPE-02 | Agregar productos al carrito de compras | Alta |
| RPE-03 | Realizar pedido desde la plataforma | Alta |
| RPE-04 | Visualizar estado del pedido | Media |
| RPE-05 | Cancelar pedido antes de ser despachado | Media |

---

## 4. Gestión de Usuarios y Roles

### 4.1 Módulo de Autenticación

| ID | Requerimiento | Prioridad |
|----|--------------|-----------|
| RU-01 | Registrar usuario con credenciales (email, contraseña) | Alta |
| RU-02 | Iniciar sesión con validación de credenciales | Alta |
| RU-03 | Cerrar sesión | Alta |
| RU-04 | Recuperar contraseña por email | Media |
| RU-05 | Cambiar contraseña desde el perfil | Media |

### 4.2 Módulo de Roles

| ID | Requerimiento | Prioridad |
|----|--------------|-----------|
| ROL-01 | Definir roles: Cliente, Personal Veterinario, Administrador | Alta |
| ROL-02 | Asignar rol al usuario al momento del registro o edición | Alta |
| ROL-03 | Control de acceso según el rol asignado | Alta |
| ROL-04 | Visualizar listado de usuarios por rol | Media |
| ROL-05 | Desactivar usuario del sistema | Media |

### 4.3 Permisos por Rol

| Funcionalidad | Cliente | Personal Veterinario | Administrador |
|--------------|---------|---------------------|---------------|
| Registrar mascota | ✅ | ✅ | ✅ |
| Solicitar cita | ✅ | ❌ | ✅ |
| Gestionar citas | ❌ | ✅ | ✅ |
| Ver historial clínico | ✅ (propias) | ✅ | ✅ |
| Registrar producto | ❌ | ❌ | ✅ |
| Gestionar inventario | ❌ | ✅ | ✅ |
| Realizar pedidos | ✅ | ❌ | ✅ |
| Gestionar usuarios | ❌ | ❌ | ✅ |
| Ver reportes | ❌ | ✅ | ✅ |

---

## 5. Requerimientos No Funcionales

| ID | Requerimiento | Categoría |
|----|--------------|-----------|
| RNF-01 | El sistema debe ser responsive (adaptado a móviles y escritorio) | Usabilidad |
| RNF-02 | Tiempo de respuesta máximo de 3 segundos | Rendimiento |
| RNF-03 | Contraseñas almacenadas con hash y sal | Seguridad |
| RNF-04 | Sesiones con expiración automática | Seguridad |
| RNF-05 | Respaldo automático de la base de datos | Disponibilidad |
| RNF-06 | Compatible con navegadores Chrome, Firefox, Safari, Edge | Compatibilidad |
| RNF-07 | Interfaz intuitiva y fácil de usar | Usabilidad |

---

## 6. Tecnologías Propuestas

| Capa | Tecnología |
|------|-----------|
| Frontend | React / Vue.js / Angular |
| Backend | Node.js / Python / Java |
| Base de datos | PostgreSQL / MySQL |
| Autenticación | JWT / OAuth 2.0 |
| Hosting | Vercel / AWS / DigitalOcean |

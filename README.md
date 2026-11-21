# Jovan Pets

Aplicación web para la gestión de una veterinaria: clientes y mascotas, historial clínico, citas veterinarias y de peluquería, catálogo de productos, pedidos con entrega a domicilio o recogida, inventario y administración de usuarios por rol.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Frontend | React 18 + TypeScript, React Router, Vite |
| Backend | Java 21, Spring Boot 3.5 (Web, Security, Validation, OAuth2 Resource Server para JWT) |
| Persistencia | MongoDB con Spring Data MongoDB (`MongoRepository`, `MongoTemplate`) |
| Pruebas | JUnit 5, Mockito, MockMvc, MongoDB embebido (Flapdoodle) |
| Build | Maven (wrapper incluido), npm |

No se usa PostgreSQL, MySQL, Hibernate ni JPA.

## Requisitos de instalación

- JDK 21
- Node.js 20 o superior y npm
- MongoDB 6 o 7 en ejecución (local o remoto). Maven no es necesario: se usa `mvnw`.

## Configuración de MongoDB

1. Instala MongoDB Community Server y deja el servicio corriendo en `localhost:27017`, o usa una URI remota.
2. La base `jovanpets` y sus colecciones se crean solas. Los índices (únicos y de consulta) se crean al iniciar (`spring.data.mongodb.auto-index-creation=true`).
3. Colecciones: `usuarios`, `clientes`, `mascotas`, `atenciones`, `servicios`, `citas`, `productos`, `movimientos_inventario`, `pedidos`, `contadores`. El diseño se explica en [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md).

## Variables de entorno

Backend (`backend/.env.example`). Copia el archivo como `backend/.env`, que no se versiona y se carga automáticamente, o define las variables en el sistema:

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `MONGODB_URI` | Cadena de conexión | `mongodb://localhost:27017/jovanpets` |
| `JWT_SECRET` | Secreto HMAC de al menos 32 caracteres | Si falta, se genera uno temporal |
| `JWT_MINUTOS` | Duración del token; se renueva con actividad | `30` |
| `CORS_ORIGENES` | Orígenes permitidos, separados por coma | `http://localhost:5173` |
| `FRONTEND_URL` | Base del enlace de recuperación de contraseña | `http://localhost:5173` |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Administrador inicial; solo se crea si no existe ninguno | vacío |
| `APP_DATOS_INICIALES` | `true` crea servicios y productos de ejemplo si las colecciones están vacías | `false` |

Frontend (`frontend/.env.example`): `VITE_API_URL=http://localhost:8080`.

## Ejecutar el backend

```powershell
cd backend
copy .env.example .env   # edita el secreto y la contraseña del administrador
.\mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8080`. Puedes comprobarla en `GET /api/estado`, que también informa si MongoDB está disponible.

## Ejecutar el frontend

```powershell
cd frontend
copy .env.example .env
npm install
npm run dev
```

Abre `http://localhost:5173`. Para generar la versión de producción: `npm run build` (sale en `frontend/dist`).

## Abrir en Visual Studio Code

Abre la carpeta raíz `Proyecto-final` (Archivo → Abrir carpeta). Extensiones recomendadas: *Extension Pack for Java* y *Spring Boot Extension Pack* para `backend/`, y *ESLint* para `frontend/`. La clase principal es `backend/src/main/java/com/jovanpets/JovanPetsApplication.java`.

## Estructura del proyecto

```text
Proyecto-final/
├── backend/                      API REST Spring Boot (arquitectura en capas)
│   └── src/main/java/com/jovanpets/
│       ├── controller/           Capa de presentación: endpoints REST
│       ├── dto/                  Transferencia de datos: Request / Response validados
│       ├── service/              Lógica de negocio y casos de uso
│       ├── domain/               Enumeraciones y reglas puras del dominio
│       ├── document/             Documentos MongoDB (@Document)
│       ├── repository/           Acceso a datos (Spring Data MongoDB)
│       ├── mapper/               Conversión documento → DTO
│       ├── security/             JWT y usuario autenticado
│       ├── config/               Seguridad, agenda y carga inicial
│       └── exception/            Excepciones y manejador global
├── frontend/src/                 React + TypeScript
│   ├── pages/  components/  layouts/  routes/
│   ├── services/                 Comunicación con la API
│   ├── contexts/  hooks/  interfaces/  styles/  utils/
├── docs/                         Arquitectura, API, trazabilidad, diagramas, wireframes
└── tools/visual-paradigm/        Generador de los diagramas con la Open API de Visual Paradigm
```

## Funcionalidades implementadas

- **Usuarios y acceso (HU-18 a HU-22):** registro con rol Cliente, inicio y cierre de sesión con JWT, sesión que expira por inactividad, cambio de contraseña, recuperación con enlace de 24 horas, administración de roles con efecto inmediato y control de acceso por rol.
- **Clientes (HU-01 a HU-04):** registro con cédula única, edición con historial de cambios, búsqueda por nombre, cédula o teléfono, listado paginado y ordenable, y desactivación.
- **Mascotas e historial (HU-05 a HU-08):** registro asociado al propietario, ficha, antecedentes en orden cronológico inverso con filtro por fechas, vacunas y desparasitaciones con próxima dosis.
- **Citas (HU-09 a HU-11, HU-23):** disponibilidad por servicio y fecha, solicitud, confirmación o rechazo con motivo, cancelación de citas futuras, reprogramación, calendario diario y semanal con filtro por veterinario. Las agendas de veterinaria y de peluquería son independientes.
- **Servicios (HU-12):** catálogo administrable con precio, duración y estado.
- **Productos e inventario (HU-13 a HU-15):** catálogo por categorías, entradas y salidas sin stock negativo, historial de movimientos y alertas de stock bajo.
- **Pedidos (HU-16, HU-17):** carrito, total calculado en el backend, entrega a domicilio o recogida, número único, descuento de stock, estados y cancelación antes del despacho con reposición del stock.
- **Dashboard (HU-24):** clientes, mascotas, citas del día por estado, alertas de stock y pedidos recientes.

El detalle por historia está en [docs/TRAZABILIDAD.md](docs/TRAZABILIDAD.md) y los puntos pendientes en [docs/DECISIONES.md](docs/DECISIONES.md).

## Pruebas

```powershell
cd backend
.\mvnw.cmd test
```

Hay 24 pruebas: reglas de dominio y de pedidos (unitarias, con Mockito) y 13 flujos de integración contra un MongoDB embebido y aislado (`jovanpets_pruebas`). Las pruebas nunca usan la base de desarrollo. Frontend: `npm run typecheck` y `npm run build`.

## Arquitectura

Monolito en capas: **Controller → Service → Repository → MongoDB**, con DTO en la frontera de la API y documentos MongoDB separados de las reglas puras del dominio. El frontend React consume la API REST por HTTP con un token Bearer. Las cinco vistas arquitectónicas (contexto, funcional, lógica, desarrollo y despliegue) están en [docs/DIAGRAMAS.md](docs/DIAGRAMAS.md), y las decisiones de persistencia, índices y concurrencia en [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md).

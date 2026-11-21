# Arquitectura y persistencia

## Estilo: arquitectura en capas

| Capa | Paquete | Responsabilidad |
|---|---|---|
| Presentación | `controller` | Endpoints REST, validación inicial (`@Valid`), autorización por rol (`@PreAuthorize`), códigos HTTP. Sin lógica de negocio. |
| Transferencia de datos | `dto` | `*Request` validados con Bean Validation y `*Response` que controlan lo que sale (nunca se expone el hash de contraseña ni los documentos). |
| Aplicación y negocio | `service` | Casos de uso y reglas: horarios ocupados, stock, transiciones de estado, propiedad de los datos, compensaciones. |
| Dominio | `domain` | Enumeraciones y reglas puras sin dependencias de Spring ni de MongoDB (`EstadoCita`, `EstadoPedido`, `HorarioAtencion`…). Los estados con ciclo de vida implementan la interfaz `EstadoConTransiciones`. |
| Persistencia | `repository`, `document` | Interfaces `MongoRepository`, fragmento `ProductoStockOperations` con operaciones atómicas en `MongoTemplate`, `SecuenciaRepository`; documentos `@Document`. |
| Transversal | `security`, `config`, `exception`, `mapper` | JWT, CORS, carga inicial, manejo global de errores y conversión documento → DTO. |

Las dependencias van en un solo sentido (`controller → service → repository`). Los servicios que necesitan datos de otro módulo usan el servicio dueño de ese módulo (por ejemplo, `CitaService → MascotaService`), sin dependencias circulares.

## Modelo de documentos MongoDB

| Colección | Documento | Incrustado | Referencias (por id) | Índices |
|---|---|---|---|---|
| `usuarios` | `Usuario` | — | `clienteId → clientes` | `email` único, `rol`, `clienteId` (sparse) |
| `clientes` | `Cliente` | `historial[]` (`CambioCliente`) | — | `cedula` único, `nombre`, `telefono` |
| `mascotas` | `Mascota` | — | `propietarioId → clientes` | `propietarioId` |
| `atenciones` | `Atencion` | — | `mascotaId → mascotas` | `{mascotaId, fecha desc}` |
| `servicios` | `Servicio` | — | — | — |
| `citas` | `Cita` | `franjas[]` | `mascotaId`, `clienteId`, `servicioId`, `veterinarioId` | `{agenda, franjas}` **único parcial** (`bloqueaAgenda=true`), `inicio`, `clienteId`, `mascotaId` |
| `productos` | `Producto` | — | — | `nombre`, `categoria`; campo `@Version` |
| `movimientos_inventario` | `MovimientoInventario` | — | `productoId`, `pedidoId` | `{productoId, fecha desc}` |
| `pedidos` | `Pedido` | `detalles[]` (`DetallePedido`) | `clienteId` | `numero` único, `clienteId`, `fecha` |
| `contadores` | `Contador` | — | — | `_id` = nombre de la secuencia |

Los documentos con baja lógica (`Usuario`, `Cliente`, `Mascota`, `Servicio`, `Producto`) heredan de la clase abstracta `RegistroActivable`; MongoDB guarda el campo heredado `activo` dentro de cada documento.

### Incrustar o referenciar

- **Se incrusta** lo que solo se consulta junto con su dueño y tiene tamaño acotado: el historial de cambios del cliente y los detalles del pedido (composición Pedido ◆ DetallePedido, con el precio vigente al comprar).
- **Se referencia** lo que crece sin límite o se consulta por separado: mascotas, atenciones, citas, movimientos y pedidos.
- Las citas y los pedidos guardan **copias de nombres** (mascota, cliente, servicio, producto) para listar sin uniones. Son un registro histórico: si luego cambia el nombre del producto, el pedido conserva el que tenía al comprarse.
- MongoDB no aplica integridad referencial. Los servicios validan que cada referencia exista y esté activa antes de guardar (`ClienteService.obtenerActivo`, `MascotaService.obtenerActiva`, `ServicioCatalogoService.obtenerActivo`). Las bajas son lógicas (`activo=false`), así que las referencias históricas no se rompen.
- El modelo lógico (diagrama de clases) usa identificadores conceptuales; en MongoDB los `id` son `ObjectId` representados como `String`.

## Consistencia y concurrencia

El mecanismo funciona en un MongoDB independiente, sin replica set, y no requiere transacciones multi-documento.

1. **Horarios ocupados (citas):** cada cita activa guarda en `franjas` los bloques de 15 minutos que ocupa. El índice único parcial `{agenda, franjas}` hace que MongoDB rechace de forma atómica cualquier cita que comparta una franja con otra activa, aunque las dos solicitudes lleguen al mismo tiempo (`DuplicateKeyException` → HTTP 409). Al cancelar o rechazar, `bloqueaAgenda=false` saca la cita del índice y la franja queda libre (HU-23).
2. **Stock nunca negativo:** las salidas usan `findAndModify` con la condición `stock >= cantidad` y `$inc: {stock: -cantidad}` en una sola operación atómica. Si no alcanza, no se modifica nada y se responde 409.
3. **Pedido con varios productos:** se descuenta el stock producto por producto de forma atómica. Si uno falla, o falla el guardado, se **compensa** devolviendo lo ya descontado y se elimina el pedido parcial (patrón saga local). Después se registran los movimientos de salida.
4. **Doble cancelación:** `Pedido` y `Cita` tienen `@Version` (bloqueo optimista). Dos cancelaciones simultáneas no pueden reponer el stock dos veces: la segunda recibe 409.
5. **Edición de productos:** `@Version`; las operaciones de stock también incrementan la versión, así que una edición basada en datos desactualizados se rechaza.
6. **Número de pedido:** secuencia atómica `$inc` con upsert en `contadores` (`JP-AAAA-000001`) e índice único sobre `numero`.

Si el despliegue final usa un replica set, se puede reemplazar la compensación del punto 3 por `@Transactional` con `MongoTransactionManager` sin cambiar la API.

## Seguridad

- Contraseñas con BCrypt (sal aleatoria por contraseña).
- JWT HMAC-SHA256 de 30 minutos, sin estado ni cookies, por eso CSRF no aplica. El frontend renueva el token mientras hay actividad, así la sesión expira por inactividad.
- En cada petición, `ConvertidorJwt` carga el usuario desde MongoDB. Un cambio de rol tiene efecto inmediato, una cuenta desactivada pierde el acceso y el cierre de sesión invalida los tokens (`versionSesion`).
- Autorización por rol con `@PreAuthorize`, y por propiedad de los datos en los servicios: un cliente solo ve sus mascotas, citas, historial y pedidos.
- CORS restringido a `CORS_ORIGENES`. Secretos y credenciales solo por variables de entorno.
- Errores con formato uniforme `{fecha, estado, mensaje, errores}` desde `GlobalExceptionHandler`, sin trazas ni mensajes internos.

## Recorrido de una solicitud

El usuario actúa en el navegador → React llama `POST /api/pedidos` con `Authorization: Bearer <token>` → Spring Security valida la firma y carga el usuario → `PedidoController` valida el `PedidoRequest` → `PedidoService` aplica las reglas, descuenta stock en `productos` y guarda en `pedidos` y `movimientos_inventario` a través de los repositorios → el driver de MongoDB ejecuta las operaciones → el servicio devuelve un `PedidoResponse` → la API responde 201 → React muestra la confirmación.

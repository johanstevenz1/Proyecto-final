# Vistas arquitectónicas (Visual Paradigm)

El proyecto editable es [diagramas/Jovan-Pets.vpp](diagramas/Jovan-Pets.vpp), hecho en Visual Paradigm 18.1. Contiene las cinco vistas como modelos UML nativos: clases, componentes, interfaces, nodos, artefactos y relaciones son elementos del modelo, no imágenes pegadas. Las imágenes PNG se exportaron con el motor de Visual Paradigm y conservan la marca de agua de la edición de evaluación. [validacion-modelo.txt](diagramas/validacion-modelo.txt) registra el tipo y el número de elementos de cada diagrama.

| # | Vista | Tipo de diagrama | Imagen |
|---|---|---|---|
| 01 | Contexto | DFD de contexto (Yourdon/DeMarco) | [01-contexto.png](diagramas/01-contexto.png) |
| 02 | Funcional | UML de componentes | [02-funcional.png](diagramas/02-funcional.png) |
| 03 | Lógica | UML de clases del dominio | [03-logica.png](diagramas/03-logica.png) |
| 04 | Desarrollo | UML de clases organizado por capas (paquetes) | [04-desarrollo.png](diagramas/04-desarrollo.png) |
| 05 | Despliegue | UML de despliegue | [05-despliegue.png](diagramas/05-despliegue.png) |

## Correcciones según las observaciones del profesor

| Vista | Observación | Corrección |
|---|---|---|
| 01 Contexto | No todas las entidades deben tener una flecha de entrada y otra de salida; hay que analizar cada flujo. | Cliente, Administrador y Personal veterinario envían y reciben datos. El **Servicio de correo solo recibe** del sistema (el enlace de recuperación, HU-22); se eliminó el flujo de retorno que no existe. |
| 02 Funcional | No poner el sistema en el centro conectado a las partes; mostrar las partes y **cómo se conectan entre ellas**. | Ya no hay un componente central. Se muestran 14 partes con 13 interfaces provistas (círculo) y requeridas (flecha de uso). Por ejemplo, Citas requiere IMascotas, IClientes e IServicios; Pedidos requiere IInventario e IClientes; el Panel consolida cinco interfaces; las partes de negocio persisten mediante IRepositorios. |
| 03 Lógica | Todas las clases con **atributos y operaciones**, **constructores, getters y setters**; revisar los tipos de relación (**herencia, agregación, interfaces**). | Los miembros de cada clase se extraen del código Java con `tools/visual-paradigm/extraer-modelo.js`, así que coinciden con la implementación. El diagrama incluye herencia (`RegistroActivable`), realización de interfaz (`EstadoConTransiciones`), composición, agregación, asociaciones con multiplicidad y dependencias hacia las enumeraciones. |

## 01 · Vista de contexto

Diagrama de contexto con Jovan Pets como único proceso y cuatro entidades externas:

- **Cliente:** envía datos de cuenta, mascotas, solicitudes de cita y pedidos. Recibe catálogo, horarios, historial y estados.
- **Administrador:** envía clientes, servicios, productos, precios y roles. Recibe listados, confirmaciones y métricas.
- **Personal veterinario:** envía confirmaciones de citas, atenciones clínicas y movimientos de stock. Recibe agenda, historial y alertas.
- **Servicio de correo:** solo recibe el enlace temporal de recuperación de contraseña. La integración con un proveedor real está pendiente.

## 02 · Vista funcional

| Parte | Clases reales | Provee | Requiere |
|---|---|---|---|
| Frontend web | `frontend/src` | — | IApiRest |
| API REST y seguridad | `*Controller`, `ConvertidorJwt`, `TokenService`, `SecurityConfig` | IApiRest | interfaces de las partes de negocio |
| Clientes | `ClienteService` | IClientes | IRepositorios |
| Mascotas e historial clínico | `MascotaService`, `AtencionService` | IMascotas | IClientes, IRepositorios |
| Catálogo de servicios | `ServicioCatalogoService` | IServicios | IRepositorios |
| Citas y agenda | `CitaService`, `HorarioAtencion` | ICitas | IMascotas, IClientes, IServicios, IRepositorios |
| Inventario | `InventarioService` | IInventario | IRepositorios |
| Productos | `ProductoService` | IProductos | IInventario, IRepositorios |
| Pedidos | `PedidoService` | IPedidos | IInventario, IClientes, IRepositorios |
| Panel administrativo | `PanelService` | IPanel | IClientes, IMascotas, ICitas, IInventario, IPedidos, IRepositorios |
| Usuarios y acceso | `AuthService`, `UsuarioService` | IUsuarios | ServicioCorreo, IRepositorios |
| Correo (pendiente) | `ServicioCorreo`, `CorreoRegistroLocalService` | ServicioCorreo | — |
| Acceso a datos | `*Repository`, `ProductoStockOperations`, `SecuenciaRepository` | IRepositorios | Protocolo MongoDB |
| MongoDB Atlas | base `jovanpets` | Protocolo MongoDB | — |

Las dependencias corresponden a lo que cada servicio recibe por constructor en el código.

## 03 · Vista lógica

- **Clases del dominio:** Usuario, Cliente, CambioCliente, Mascota, Atencion, Servicio, Cita, Producto, MovimientoInventario, Pedido y DetallePedido, más la superclase abstracta RegistroActivable. Cada una muestra sus atributos, su constructor, getters, setters y demás operaciones, tal como están en el código.
- **Herencia:** Usuario, Cliente, Mascota, Servicio y Producto extienden `RegistroActivable`, que implementa la baja lógica (`activo`, `activar()`, `desactivar()`).
- **Interfaz:** `EstadoCita` y `EstadoPedido` realizan `EstadoConTransiciones<E>` (`siguientes()`, `puedeCambiarA()`).
- **Composición:** Pedido ◆ DetallePedido (1..*) y Cliente ◆ CambioCliente (historial).
- **Agregación:** Cliente ◇ Mascota.
- **Asociaciones con multiplicidad:** Usuario–Cliente, Mascota–Atencion, Mascota–Cita, Servicio–Cita, Usuario–Cita (atiende), Cliente–Pedido, Producto–DetallePedido, Producto–MovimientoInventario y Pedido–MovimientoInventario.
- **Dependencias «use»:** hacia las diez enumeraciones del dominio, que muestran sus literales y los métodos `values()` y `valueOf()`.

El diseño físico en MongoDB (colecciones, referencias, documentos incrustados e índices) está en [ARQUITECTURA.md](ARQUITECTURA.md).

## 04 · Vista de desarrollo

Diagrama de clases por capas siguiendo la referencia *SmartLibrary*. Cada capa es un paquete UML con el nombre real del paquete Java (`controller`, `dto`, `security`, `service`, `mapper`, `repository`, `document`, `domain`). Muestra completo el módulo de pedidos e inventario; los demás módulos repiten la estructura `XController → XService → XRepository → X`.

## 05 · Vista de despliegue

Topología publicada:

- **Equipo del usuario:** navegador web con la SPA de React.
- **Vercel:** hosting estático HTTPS en `frontend-kohl-pi-40.vercel.app` con el build de Vite.
- **Render:** contenedor Docker en `jovan-pets-api.onrender.com`, con JVM 21, Spring Boot 3.5 y el JAR del backend, configurado por variables de entorno.
- **MongoDB Atlas:** cluster M0 con réplica de tres nodos en AWS us-east-1 y la base `jovanpets`.

Conexiones: HTTPS para descargar la SPA; HTTPS con REST/JSON y token Bearer hacia el backend; `mongodb+srv` sobre TLS (puerto 27017) desde el backend hacia Atlas. El entorno local usa los mismos tres nodos en el equipo del desarrollador.

## Regenerar las vistas

1. `node tools/visual-paradigm/extraer-modelo.js backend/src/main/java/com/jovanpets tools/visual-paradigm/modelo-logico.txt` actualiza el modelo lógico desde el código.
2. `tools/visual-paradigm/VistasCorregidas.java` (complemento `plugin-correccion.xml`) regenera las cinco vistas, guarda el `.vpp` y exporta las imágenes. Instrucciones en [tools/visual-paradigm/README.md](../tools/visual-paradigm/README.md).

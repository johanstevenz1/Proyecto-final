# Vistas arquitectónicas (Visual Paradigm)

El proyecto editable es [diagramas/Jovan-Pets.vpp](diagramas/Jovan-Pets.vpp), hecho en Visual Paradigm 18.1, con las cinco vistas como modelos UML nativos: clases, paquetes, nodos, artefactos y relaciones son elementos del modelo, no imágenes pegadas. Las imágenes PNG se exportaron con el motor de Visual Paradigm y conservan la marca de agua de la edición de evaluación. [validacion-modelo.txt](diagramas/validacion-modelo.txt) registra el tipo y el número de elementos de cada diagrama.

| # | Vista | Tipo de diagrama | Imagen |
|---|---|---|---|
| 01 | Contexto | DFD de contexto (Yourdon/DeMarco) | [01-contexto.png](diagramas/01-contexto.png) |
| 02 | Funcional | UML de componentes | [02-funcional.png](diagramas/02-funcional.png) |
| 03 | Lógica | UML de clases del dominio | [03-logica.png](diagramas/03-logica.png) |
| 04 | Desarrollo | UML de clases organizado por capas (paquetes) | [04-desarrollo.png](diagramas/04-desarrollo.png) |
| 05 | Despliegue | UML de despliegue | [05-despliegue.png](diagramas/05-despliegue.png) |

Las vistas 01 a 03 vienen de la entrega anterior y no se modificaron. Las vistas 04 y 05 son nuevas y se construyeron a partir del código actual.

## 04 · Vista de desarrollo

Sigue el formato de la referencia *SmartLibrary* (Controller → Service → Repository → Model): un diagrama de clases donde cada capa es un paquete UML con el nombre real del paquete Java (`com.jovanpets.<capa>`). Muestra completo el módulo de pedidos e inventario, porque es el que coordina más capas. Los demás módulos (clientes, mascotas, citas, servicios, usuarios) repiten la misma estructura `XController → XService → XRepository → X`.

- **controller:** `PedidoController` («RestController») con sus seis endpoints y los DTO que recibe y devuelve.
- **dto:** `PedidoRequest`, `LineaPedidoRequest`, `PedidoResponse` («record», validados con Bean Validation).
- **security:** `UsuarioAutenticado`, el actor que llega a cada operación.
- **service:** `PedidoService` e `InventarioService` («Service») con los atributos inyectados y las operaciones reales; `-cargarVendibles` y `-verificarAcceso` son privadas.
- **mapper:** `PedidoMapper`, que convierte documentos en DTO.
- **repository:** `PedidoRepository`, `ProductoRepository` (hereda de `ProductoStockOperations`, cuyas operaciones son atómicas), `MovimientoInventarioRepository` y `SecuenciaRepository`.
- **document:** `Pedido` ◆ `DetallePedido` (composición incrustada), `Producto`, `MovimientoInventario` («Document»).
- **domain:** `EstadoPedido` (con `puedeCambiarA` y `esCancelable`) y `ModalidadEntrega`.

Notación: las flechas continuas son asociaciones dirigidas (atributos inyectados por constructor), las punteadas «use»/«create» son dependencias, el triángulo es generalización (interfaz que extiende otra) y el rombo lleno es composición.

## 05 · Vista de despliegue

- **«device» Equipo del usuario → «executionEnvironment» Navegador web → «artifact» Jovan Pets SPA** (React + TypeScript compilado con Vite).
- **«device» Servidor web del frontend → servidor de archivos estáticos** (en desarrollo, Vite en el puerto 5173) **→ «artifact» frontend/dist**.
- **«device» Servidor de aplicación → JVM Java 21 → Spring Boot 3.5 con Tomcat embebido en el puerto 8080 → «artifact» jovan-pets-backend-1.0.0.jar**, más el «deployment spec» `application.properties` con las variables de entorno.
- **«device» Servidor de base de datos → MongoDB 7.0 (puerto 27017) → «artifact» base jovanpets**, con sus colecciones e índices únicos.

Vías de comunicación: navegador ↔ frontend por HTTP/HTTPS para descargar la SPA; navegador → backend por HTTP/HTTPS en el puerto 8080 con REST/JSON y token Bearer JWT; backend → MongoDB por TCP 27017 con el MongoDB Wire Protocol y el driver Java. La nota inferior describe el recorrido de una solicitud y separa el entorno local (todo en localhost) del despliegue final, cuyo proveedor aún no se ha elegido.

## Regenerar las vistas

`tools/visual-paradigm/VistasDesarrolloDespliegue.java` (complemento `plugin-vistas.xml`) abre el proyecto base, reemplaza las vistas 04 y 05, guarda el `.vpp` y exporta las cinco imágenes. Instrucciones en [tools/visual-paradigm/README.md](../tools/visual-paradigm/README.md).

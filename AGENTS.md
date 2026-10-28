# AGENTS.md — Proyecto Jovan Pets

## 1. Propósito del proyecto

Jovan Pets es el proyecto final de Ingeniería de Software. Se busca construir una plataforma web para centralizar y mejorar la gestión de un negocio veterinario familiar: clientes, mascotas, citas veterinarias, peluquería, catálogo de productos y medicamentos, pedidos, entregas o recogidas e inventario.

El sistema debe reducir el manejo manual de la información, mantener los datos organizados y ofrecer una base que pueda ampliarse sin rehacer toda la aplicación.

Repositorio de referencia: https://github.com/johanstevenz1/Proyecto-final

## 2. Regla principal: revisar el repositorio antes de modificarlo

Antes de escribir o reemplazar código:

1. Inspecciona la estructura completa del repositorio y determina qué tecnologías y funcionalidades ya existen.
2. Localiza las historias de usuario (HU), los wireframes, las reglas de negocio, la documentación y cualquier diagrama existente.
3. Relaciona cada HU con las pantallas y elementos de los wireframes que la respaldan.
4. Identifica qué partes ya funcionan, cuáles están incompletas y cuáles contradicen los requisitos.
5. Conserva lo que sea válido y modifica el código existente cuando sea necesario. No reconstruyas todo desde cero sin una razón técnica clara.
6. No inventes historias de usuario, pantallas, reglas de negocio ni funciones que no estén sustentadas por la documentación. Si falta información, registra la duda y propone una decisión explícita para revisión.

El análisis debe basarse en los archivos reales del repositorio. No afirmes haber revisado un wireframe o una HU si no se encontró y leyó.

## 3. Prioridades de trabajo

### Prioridad inmediata: análisis y los tres primeros diagramas

En esta etapa, céntrate en producir entregables coherentes y verificables para:

1. **Vista de contexto:** actores externos, sistema Jovan Pets, objetivos y límites del sistema. Debe corresponder directamente con las HU.
2. **Vista funcional:** diagrama UML de componentes que muestre los módulos o responsabilidades funcionales del sistema.
3. **Vista lógica:** diagrama UML de clases del dominio, con atributos, operaciones relevantes y relaciones justificadas.

Estos tres diagramas son los entregables prioritarios para la próxima clase. Antes de elaborarlos, revisa las HU y wireframes existentes.

### Implementación técnica prevista

La solución final contempla frontend, backend y base de datos. La primera implementación debe dar prioridad a un backend funcional en **Java con Spring Boot** y a su persistencia en una base de datos. El frontend y el despliegue se abordarán de forma coherente con el estado real del repositorio y los requisitos de la entrega.

Vercel es una opción de despliegue mencionada, pero no es la prioridad inmediata. Antes de elegir la estrategia de despliegue, comprueba si encaja con el frontend y el backend elegidos; no supongas que un backend Spring Boot puede ejecutarse directamente en cualquier configuración de Vercel.

## 4. Tecnologías y estilo de código

- Backend: Java y Spring Boot.
- Persistencia: base de datos relacional, salvo que los archivos del proyecto justifiquen otra opción. Evalúa la tecnología existente antes de cambiarla.
- Acceso a datos: Spring Data JPA si es compatible con la estructura del proyecto.
- API: endpoints REST cuando correspondan al diseño existente.
- Validación: Jakarta Bean Validation y validaciones de negocio en la capa apropiada.
- Pruebas: pruebas unitarias para reglas de negocio y pruebas de integración para los flujos importantes.
- Idioma: los nombres de clases, métodos, atributos, tablas y comentarios pueden estar en español para facilitar el aprendizaje y la exposición. Mantén una convención consistente; no mezcles idiomas sin necesidad.
- Usa nombres descriptivos, código legible y soluciones adecuadas al nivel académico del proyecto.
- No agregues dependencias o herramientas innecesarias.

No cambies de framework, base de datos ni estructura del proyecto solo por preferencia. Explica las razones de cualquier cambio importante.

## 5. Requisitos funcionales del dominio

Los siguientes módulos constituyen el contexto inicial del negocio. Deben contrastarse con las HU y los wireframes del repositorio antes de considerarse requisitos confirmados.

### 5.1 Usuarios, clientes y permisos
- Registrar y administrar cuentas y datos de clientes.
- Diferenciar los permisos de clientes y personal autorizado.
- Proteger las funciones administrativas mediante autenticación y autorización.
- No almacenar contraseñas en texto plano.

### 5.2 Mascotas
- Registrar mascotas vinculadas a sus responsables.
- Guardar datos identificativos como nombre, especie, raza y edad, según lo que indiquen las HU.
- Consultar las mascotas para relacionarlas con citas y servicios.
- Incluir historial de atenciones solo si está dentro del alcance confirmado.

### 5.3 Citas veterinarias
- Consultar servicios y solicitar citas.
- Registrar fecha, hora, mascota, cliente y estado cuando corresponda.
- Permitir al personal autorizado confirmar, reprogramar o cancelar citas.
- Validar la disponibilidad y evitar reservas incompatibles si las reglas de negocio lo requieren.

### 5.4 Peluquería
- Consultar servicios de peluquería y solicitar reservas.
- Vincular cada reserva con el cliente y la mascota.
- Permitir al personal autorizado gestionar horarios y estados.
- Reutilizar conceptos comunes con las citas solo cuando el modelo resultante siga siendo claro y corresponda a las HU.

### 5.5 Productos y medicamentos
- Mantener un catálogo de productos, categorías, descripción, precio y existencias.
- Permitir al personal autorizado crear, consultar, modificar y desactivar productos.
- Registrar imágenes solo si el diseño y la infraestructura elegidos las necesitan.
- Tratar los medicamentos como productos del catálogo, sin añadir funciones clínicas o de prescripción que no estén solicitadas.

### 5.6 Carrito y pedidos
- Permitir seleccionar productos y cantidades.
- Registrar los detalles, precios aplicados y total de un pedido.
- Consultar el estado del pedido.
- Permitir al personal autorizado actualizar los estados según el proceso definido.
- Mantener consistencia entre pedidos, detalles de pedido, productos e inventario.
- No implementar pagos electrónicos hasta confirmar que forman parte del alcance.

### 5.7 Entrega o recogida
- Permitir seleccionar entrega a domicilio o recogida en el establecimiento si así lo confirman las HU.
- Guardar solo los datos necesarios para la modalidad elegida.
- Representar los estados del pedido de forma coherente y evitar estados contradictorios.

### 5.8 Inventario
- Registrar y consultar existencias.
- Actualizar cantidades mediante operaciones controladas.
- Mantener consistencia entre las ventas y las existencias.
- Considerar alertas de stock bajo solo si el alcance lo permite.
- Evitar que dos operaciones concurrentes dejen el inventario en un estado inválido.

### 5.9 Panel administrativo
- Reunir accesos a los módulos que correspondan al rol del usuario.
- No implementar reportes avanzados si no son necesarios para la primera versión.

## 6. Arquitectura del backend: capas

Organiza el backend con una arquitectura por capas y responsabilidades separadas. Ajusta los nombres y paquetes a la estructura real del repositorio, manteniendo una separación equivalente a la siguiente:

- **Controladores (`controlador`):** reciben peticiones HTTP, validan la entrada de transporte y devuelven respuestas. No deben contener reglas de negocio complejas.
- **Servicios (`servicio`):** contienen los casos de uso y reglas de negocio; coordinan repositorios y operaciones transaccionales.
- **Repositorios (`repositorio`):** encapsulan el acceso a la base de datos mediante Spring Data JPA u otra solución justificada.
- **Entidades (`modelo` o `entidad`):** representan los datos persistidos y sus relaciones.
- **DTO (`dto`):** definen los datos de entrada y salida de la API. Evita exponer entidades directamente si ello provoca acoplamiento o problemas de serialización.
- **Configuración y seguridad (`configuracion` / `seguridad`):** configuración transversal, autenticación y autorización cuando se implementen.
- **Excepciones (`excepcion`):** manejo consistente de errores y respuestas comprensibles.

Flujo esperado de una petición:

`Cliente HTTP / Frontend → Controlador → Servicio → Repositorio → Base de datos`

La respuesta vuelve a través de las mismas capas. Las dependencias del código deben apuntar hacia responsabilidades internas; los controladores no deben acceder directamente a la base de datos ni los repositorios decidir reglas del negocio.

Usa transacciones en operaciones que deban mantenerse atómicas, especialmente cuando se creen pedidos y se actualicen existencias.

## 7. Vista de contexto

El diagrama de contexto debe:

- Mostrar Jovan Pets como un único sistema dentro de su entorno.
- Incluir solo actores externos respaldados por las HU y los wireframes; por ejemplo, cliente y personal administrativo si realmente aparecen en los requisitos.
- Expresar qué objetivo cumple cada actor y cómo interactúa con el sistema.
- Mantener el mismo alcance y vocabulario de las historias de usuario.
- No mostrar clases, tablas, paquetes ni detalles internos del backend.
- No presentar como actores independientes los módulos internos de Jovan Pets.

Acompaña el diagrama con una breve explicación de sus límites y de las HU que lo sustentan.

## 8. Vista funcional: diagrama UML de componentes

El diagrama debe representar las responsabilidades funcionales o componentes relevantes identificados a partir de los requisitos y del código. Puede incluir, según lo que se confirme, gestión de usuarios, mascotas, citas, peluquería, catálogo, pedidos e inventario.

Reglas obligatorias:

- **No dibujar relaciones de dependencia entre componentes.** Esta es una restricción explícita de la entrega.
- No usar flechas de dependencia entre cajas de componentes.
- Mantener el diagrama legible, con nombres claros y responsabilidades delimitadas.
- No confundir componentes funcionales con actores, tablas de base de datos o clases individuales.
- Si se necesita explicar la comunicación entre módulos, hacerlo mediante una leyenda o texto complementario sin introducir las dependencias prohibidas en el diagrama.
- Verificar que todos los componentes se justifiquen por una HU, un wireframe o una responsabilidad real del código.

## 9. Vista lógica: diagrama UML de clases

El diagrama lógico debe mostrar las clases principales del dominio y las relaciones que se deduzcan de las HU, los wireframes y las reglas de negocio.

Criterios:

- Incluir clases relevantes, atributos significativos, operaciones cuando aporten claridad y multiplicidades cuando estén justificadas.
- Considerar entidades candidatas como Usuario, Cliente, Mascota, Cita, Servicio, ReservaPeluqueria, Producto, Categoria, Pedido, DetallePedido e Inventario, pero no incluirlas automáticamente: confirma el modelo antes de decidir.
- Diferenciar herencia, asociación, composición y agregación; usa cada relación solo cuando represente el dominio real.
- Evitar duplicar conceptos o crear relaciones circulares innecesarias.
- Mantener coherencia entre el diagrama, las entidades Java y el esquema de la base de datos.
- No incluir clases de framework ni detalles de infraestructura que distraigan del modelo lógico.

## 10. Historias de usuario y wireframes: trazabilidad obligatoria

Crea una matriz de trazabilidad antes de finalizar los diagramas. Para cada HU disponible, registra:

| Campo | Contenido esperado |
|---|---|
| Identificador | Código original de la HU |
| Necesidad | Resumen fiel de lo que solicita |
| Actor | Rol que inicia la acción |
| Wireframe | Pantalla o archivo relacionado |
| Funcionalidad | Módulo responsable |
| Clases / datos | Clases o entidades involucradas |
| Estado | Cubierta, parcial, no cubierta o pendiente de aclaración |

No cambies los identificadores ni el sentido de las HU. Si un wireframe no tiene una HU asociada, señálalo. Si una HU no tiene pantalla relacionada, indícalo también. No inventes cobertura para que la matriz parezca completa.

## 11. Base de datos

- Diseña un esquema relacional que refleje las entidades y reglas confirmadas.
- Define claves primarias, claves foráneas, restricciones `NOT NULL`, unicidad e índices cuando se justifiquen.
- Evita guardar el mismo dato de forma redundante sin motivo.
- Usa tipos adecuados para dinero, fechas, horas, cantidades y estados.
- Define estados como enumeraciones o valores controlados cuando sea apropiado.
- Documenta las decisiones relevantes y mantén las relaciones alineadas con el modelo de clases.
- No guardes secretos ni credenciales de conexión en el repositorio. Usa variables de entorno y proporciona un archivo de ejemplo sin valores sensibles.

## 12. Seguridad, validación y calidad

- Validar entradas del usuario en los límites de la API y validar reglas de negocio en los servicios.
- Aplicar autorización por rol a las operaciones administrativas.
- Manejar errores de forma consistente sin exponer trazas, contraseñas ni detalles sensibles.
- Evitar exponer datos personales que no sean necesarios.
- No confiar en los precios, totales, permisos o cantidades enviados por el frontend; recalcular o comprobarlos en el backend.
- Proteger la integridad del inventario y de los pedidos.
- Añadir pruebas a los casos de uso y a los errores previsibles.
- No afirmar que una función funciona si no se ha ejecutado o probado.

## 13. Coherencia entre requisitos, diagramas y código

Todo cambio debe mantener la misma lógica en estos artefactos:

1. Historias de usuario.
2. Wireframes.
3. Vista de contexto.
4. Vista funcional de componentes.
5. Vista lógica de clases.
6. Código Java y endpoints.
7. Modelo y esquema de base de datos.
8. Pruebas y documentación.

Si se descubre una contradicción, no la ocultes: describe el conflicto, identifica los archivos afectados y propone una corrección. Modifica el código cuando sea necesario para cumplir los requisitos acordados, no solo para que el diagrama se vea bien.

## 14. Vistas posteriores

Cuando los tres primeros diagramas estén revisados, prepara las vistas restantes solicitadas para la entrega:

- **Vista de desarrollo:** diagrama de clases o paquetes que muestre cómo se aplica la arquitectura por capas en el código real.
- **Vista de despliegue:** diagrama y explicación de cómo se ejecutan y se comunican frontend, backend y base de datos en el entorno local y, más adelante, en producción.

La vista de despliegue debe distinguir lo que ya está implementado de lo que solo se propone. No declares que el sistema está desplegado en Vercel hasta que el despliegue se haya realizado y verificado.

## 15. Preparación de la exposición

La exposición debe explicar el sistema de forma sencilla y coherente:

1. Qué problema de Jovan Pets se busca resolver.
2. Qué actores lo utilizan y qué necesitan, relacionándolo con las HU.
3. Cómo el diagrama de contexto delimita el sistema.
4. Cómo se organizaron las responsabilidades funcionales.
5. Cómo las clases representan la información y las relaciones del negocio.
6. Cómo se aplica la arquitectura por capas en el código Java.
7. Cómo se guarda y protege la información en la base de datos.
8. Qué funciona actualmente, qué se probó y qué queda pendiente.

Utiliza el mismo nombre para una funcionalidad en la documentación, los diagramas, el código y la exposición. Explica cada decisión con palabras que el estudiante pueda defender y comprender; no agregues complejidad solo para hacer que la arquitectura parezca más avanzada.

## 16. Forma de trabajar y entregables

Para cada fase:

1. Resume brevemente qué encontraste en el repositorio.
2. Presenta los problemas de coherencia detectados.
3. Propón un plan pequeño y priorizado.
4. Implementa cambios acotados, conservando lo que funciona.
5. Ejecuta las pruebas o comandos de verificación disponibles.
6. Resume archivos modificados, decisiones, pruebas realizadas y pendientes.

Entregables inmediatos recomendados:

- Inventario de HU y wireframes encontrados.
- Matriz de trazabilidad HU–wireframes–módulos–clases.
- Diagrama de contexto.
- Diagrama UML de componentes sin relaciones de dependencia.
- Diagrama lógico UML de clases.
- Breve justificación de cada diagrama y lista de dudas por resolver.

No inventes resultados de pruebas, archivos, historias de usuario, diagramas ni despliegues. Si no puedes acceder a un archivo, explica cuál falta y solicita el archivo o el contenido necesario.

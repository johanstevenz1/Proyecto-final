# AGENTS.md --- Proyecto Jovan Pets

## 1. Contexto general del proyecto

Desarrollar una aplicación web llamada **Jovan Pets**, orientada a la
gestión y administración de una veterinaria y sus servicios para
mascotas.

El objetivo es digitalizar y facilitar los procesos del establecimiento.
Los clientes podrán consultar servicios, registrar sus mascotas,
solicitar citas y realizar pedidos. El personal autorizado podrá
administrar citas, productos, pedidos, inventario y demás operaciones
del negocio.

La aplicación debe ser un sistema funcional, no solamente una interfaz
visual. Todas las funcionalidades deben estar conectadas con un backend
desarrollado en Java con Spring Boot y una base de datos MongoDB.

### Objetivos principales

-   Facilitar la programación y administración de citas.
-   Gestionar los servicios de peluquería para mascotas.
-   Gestionar las citas de atención veterinaria.
-   Permitir la consulta y solicitud de medicamentos y productos.
-   Administrar pedidos con opción de entrega a domicilio o recogida en
    el establecimiento.
-   Gestionar el inventario y catálogo de productos.
-   Facilitar la administración general del negocio.

------------------------------------------------------------------------

## 2. Tecnologías obligatorias

Utilizar las siguientes tecnologías, salvo que el repositorio ya
contenga una configuración aprobada que deba conservarse. No sustituir
las tecnologías obligatorias sin autorización.

### Backend

-   Java.
-   Spring Boot.
-   Spring Web para crear servicios REST.
-   Spring Data MongoDB para la persistencia.
-   Spring Security para autenticación y autorización.
-   Maven para la gestión de dependencias.
-   Bean Validation para validar datos de entrada.

### Frontend

-   React con TypeScript.
-   Componentes reutilizables.
-   Consumo de los servicios REST del backend.
-   Manejo correcto de estados de carga, errores y respuestas del
    servidor.
-   Diseño adaptable a escritorio, tabletas y dispositivos móviles.

Si el repositorio ya tiene un frontend funcional con otra tecnología,
analizarlo antes de reemplazarlo y solicitar autorización si el cambio
afecta el alcance.

### Base de datos --- MongoDB obligatorio

-   Utilizar **MongoDB** como único motor de base de datos de la
    aplicación.
-   Utilizar **Spring Data MongoDB** para la conexión y persistencia.
-   Utilizar `MongoRepository` o las abstracciones adecuadas de Spring
    Data MongoDB.
-   Modelar la información mediante documentos y colecciones.
-   Utilizar `@Document` para definir documentos y `@Id` para sus
    identificadores.
-   Configurar la conexión mediante variables de entorno o configuración
    externa.
-   Definir índices cuando sean necesarios, especialmente para campos de
    búsqueda frecuente o que deban ser únicos.
-   Decidir cuidadosamente cuándo conviene incrustar documentos y cuándo
    utilizar referencias entre documentos.
-   Evitar duplicación innecesaria y mantener consistencia en las
    operaciones.

**No utilizar PostgreSQL, MySQL, Hibernate ni Spring Data JPA como
tecnologías de persistencia.** No crear una arquitectura híbrida con
otra base de datos relacional, salvo autorización expresa.

------------------------------------------------------------------------

## 3. Análisis previo obligatorio

Antes de modificar o crear código:

1.  Revisar todos los archivos y carpetas existentes del repositorio.
2.  Identificar historias de usuario, requisitos funcionales y no
    funcionales.
3.  Revisar los wireframes y diseños disponibles.
4.  Identificar tecnologías y configuraciones que ya estén
    implementadas.
5.  Detectar funcionalidades existentes para evitar duplicarlas o
    romperlas.
6.  Identificar actores, permisos y procesos del negocio.
7.  Elaborar una matriz que relacione historias de usuario, módulos,
    endpoints, pantallas, datos y pruebas.
8.  Señalar contradicciones, requisitos incompletos y decisiones que
    necesiten confirmación.

No inventar requisitos ni asumir que una propuesta inicial es una
decisión definitiva. Si falta información que afecte el alcance o las
reglas del negocio, documentar la duda y solicitar aclaración antes de
implementar esa parte.

------------------------------------------------------------------------

## 4. Funcionalidades del sistema

Los siguientes módulos representan el alcance inicial. Deben
contrastarse con las historias de usuario y wireframes disponibles antes
de implementarlos.

### 4.1. Gestión de usuarios

Permitir:

-   Registrar usuarios.
-   Iniciar y cerrar sesión.
-   Consultar y actualizar información personal.
-   Administrar roles y permisos.
-   Diferenciar clientes y personal administrativo.
-   Restringir las operaciones según el rol autenticado.

Considerar los roles que realmente requiera el negocio, por ejemplo:
administrador, cliente, veterinario y personal de peluquería. No crear
roles innecesarios si las historias de usuario no los contemplan.

### 4.2. Gestión de mascotas

Permitir:

-   Registrar mascotas asociadas a un cliente.
-   Consultar información de las mascotas.
-   Actualizar sus datos.
-   Asociar mascotas con citas veterinarias y servicios de peluquería.

Información sugerida, sujeta a los requisitos:

-   Nombre.
-   Especie.
-   Raza.
-   Edad o fecha de nacimiento.
-   Peso.
-   Información adicional relevante.

### 4.3. Gestión de peluquería

Permitir:

-   Consultar los servicios de peluquería disponibles.
-   Programar citas.
-   Seleccionar la mascota.
-   Consultar fechas y horarios disponibles.
-   Consultar el estado de una cita.
-   Cancelar o reprogramar citas de acuerdo con las reglas del negocio.
-   Administrar las citas desde el panel correspondiente.

### 4.4. Gestión veterinaria

Permitir:

-   Programar citas veterinarias.
-   Asociar las citas con una mascota y su propietario.
-   Registrar el motivo de la consulta.
-   Consultar el historial de citas.
-   Administrar horarios y estados de atención.

La información clínica debe manejarse con permisos adecuados. No exponer
información privada a usuarios que no estén autorizados.

### 4.5. Catálogo de productos y medicamentos

Permitir:

-   Visualizar productos disponibles.
-   Consultar información y precios.
-   Consultar disponibilidad.
-   Clasificar productos por categorías.
-   Administrar el catálogo desde el panel administrativo.

No implementar venta de productos que requieran controles especiales sin
revisar previamente las reglas aplicables al negocio.

### 4.6. Gestión de pedidos

Permitir:

-   Crear pedidos de productos.
-   Agregar y eliminar productos del carrito.
-   Calcular el valor total desde el backend.
-   Seleccionar entrega a domicilio o recogida en el establecimiento.
-   Consultar el estado del pedido.
-   Consultar el historial de pedidos.
-   Administrar los pedidos desde el panel administrativo.

### 4.7. Gestión de inventario

Permitir:

-   Registrar productos.
-   Actualizar cantidades disponibles.
-   Consultar existencias.
-   Registrar entradas y salidas.
-   Actualizar precios.
-   Identificar productos con pocas unidades.
-   Mantener el inventario sincronizado con los pedidos.

Las operaciones que modifiquen el inventario deben protegerse contra
actualizaciones concurrentes y cantidades negativas. Elegir un mecanismo
compatible con la configuración de MongoDB y documentarlo.

------------------------------------------------------------------------

## 5. Arquitectura de software y vistas requeridas

El proyecto debe implementar una arquitectura organizada, documentada y
coherente con el código desarrollado.

Se deben elaborar los siguientes diagramas en **Visual Paradigm**. Los
diagramas deben representar la implementación real, no una arquitectura
idealizada que luego no exista en el código.

### 5.1. Vista de contexto

Elaborar un diagrama de contexto que represente el sistema Jovan Pets y
su interacción con los actores externos.

Considerar, de acuerdo con las historias de usuario:

-   Cliente.
-   Administrador.
-   Veterinario.
-   Personal de peluquería.
-   Sistema Jovan Pets.

Representar las interacciones principales de cada actor con el sistema.
Por ejemplo:

-   El cliente registra y consulta sus mascotas.
-   El cliente solicita citas.
-   El cliente consulta productos.
-   El cliente realiza y consulta pedidos.
-   El administrador gestiona usuarios, productos, inventario y pedidos.
-   El veterinario administra las atenciones que le correspondan.
-   El personal de peluquería gestiona los servicios y sus citas.

La base de datos es un elemento interno de soporte y no debe
representarse como actor externo del contexto.

El diagrama debe coincidir con las historias de usuario. No incluir
funcionalidades que no estén contempladas en los requisitos aprobados.

### 5.2. Vista funcional --- Diagrama UML de componentes

Elaborar un diagrama UML de componentes que represente los módulos
principales y sus dependencias.

Considerar, según la implementación:

-   Frontend web.
-   API REST.
-   Módulo de autenticación y autorización.
-   Módulo de usuarios.
-   Módulo de mascotas.
-   Módulo de citas.
-   Módulo veterinario.
-   Módulo de peluquería.
-   Módulo de productos.
-   Módulo de pedidos.
-   Módulo de inventario.
-   Acceso a datos.
-   MongoDB.

Representar las interfaces proporcionadas y requeridas cuando
corresponda. Mostrar cómo el frontend consume la API REST, cómo los
componentes del backend colaboran y cómo la capa de persistencia accede
a MongoDB.

No dibujar componentes que no existan o que no estén previstos en los
requisitos aprobados.

### 5.3. Vista lógica --- Diagrama UML de clases

Elaborar un diagrama UML de clases que represente el modelo lógico del
dominio.

Considerar inicialmente clases o conceptos como:

-   Usuario.
-   Rol.
-   Mascota.
-   Servicio.
-   Cita.
-   CitaVeterinaria.
-   CitaPeluqueria.
-   Producto.
-   Categoria.
-   Pedido.
-   DetallePedido.
-   MovimientoInventario.
-   Direccion.

Esta lista es una propuesta inicial. Analizar las historias de usuario y
evitar clases duplicadas o innecesarias.

El diagrama debe incluir, cuando corresponda:

-   Atributos y tipos.
-   Operaciones relevantes.
-   Asociaciones.
-   Multiplicidades.
-   Herencia cuando esté justificada.
-   Composición o agregación cuando represente correctamente el dominio.

**Importante:** el diagrama de clases representa el dominio lógico y no
debe confundirse con el diseño físico de colecciones MongoDB. Documentar
por separado las decisiones de persistencia: documentos incrustados,
referencias, identificadores e índices.

### 5.4. Vista de desarrollo --- Arquitectura por capas

Elaborar un diagrama UML que represente la organización del código y la
aplicación de un estilo arquitectónico por capas.

**Estilo arquitectónico obligatorio: arquitectura en capas.**

Organizar el backend con responsabilidades claras:

**Capa de presentación** - Controllers. - Endpoints REST. - Recepción de
solicitudes HTTP. - Validación inicial de datos. - Conversión de
solicitudes y respuestas.

**Capa de aplicación y lógica de negocio** - Services. - Casos de uso. -
Reglas de negocio. - Coordinación de operaciones.

**Capa de dominio** - Modelos y objetos del dominio. - Reglas e
invariantes del negocio. - Enumeraciones y tipos propios del dominio.

**Capa de persistencia** - Repositories. - Interfaces de acceso a
datos. - Consultas y operaciones sobre MongoDB. - Mapeo de documentos
cuando sea necesario.

**Capa de transferencia de datos** - DTOs. - Request. - Response. -
Validaciones de entrada y salida.

Las capas pueden organizarse en paquetes separados o módulos por
funcionalidad, pero sus responsabilidades deben permanecer claras.

Reglas obligatorias:

-   Los controllers no deben contener lógica de negocio.
-   Los services deben centralizar las reglas del negocio.
-   Los repositories deben encargarse del acceso a datos.
-   No exponer directamente documentos de persistencia en todas las
    respuestas de la API.
-   Utilizar DTOs para controlar la información que entra y sale del
    sistema.
-   Evitar dependencias circulares.
-   No permitir que el frontend acceda directamente a MongoDB.
-   Mantener la lógica de negocio independiente de los detalles de
    persistencia en la medida que sea razonable para el tamaño del
    proyecto.

El diagrama debe corresponder con los paquetes y clases que realmente
existan.

### 5.5. Vista de despliegue --- Diagrama UML de despliegue

Elaborar un diagrama UML de despliegue que explique cómo se ejecuta y
comunica el software.

Representar como mínimo:

**Dispositivo del usuario** - Navegador web. - Aplicación frontend.

**Servidor de aplicación** - Aplicación backend Java. - Spring Boot. -
API REST. - Servicios y lógica de negocio.

**Servidor de base de datos** - MongoDB. - Base de datos y colecciones
persistentes.

Representar las conexiones:

-   Navegador hacia frontend mediante HTTP/HTTPS, según el entorno.
-   Frontend hacia API REST mediante HTTP/HTTPS.
-   Backend hacia MongoDB mediante el driver de MongoDB y el protocolo
    correspondiente.

Explicar el recorrido de una solicitud: el usuario interactúa con el
frontend, el frontend envía una petición al backend, Spring Boot procesa
las reglas de negocio, consulta o modifica MongoDB y devuelve una
respuesta al frontend.

Distinguir la configuración de desarrollo local de la configuración de
despliegue final. No inventar proveedores de hosting ni servicios
externos que no hayan sido seleccionados.

------------------------------------------------------------------------

## 6. Organización del proyecto

Mantener una estructura clara y modular. Adaptar la estructura a las
convenciones existentes del repositorio cuando sea necesario, sin
mezclar responsabilidades.

### Backend

Estructura de referencia:

``` text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/jovanpets/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── domain/
│   │   │   ├── document/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   ├── security/
│   │   │   ├── mapper/
│   │   │   ├── exception/
│   │   │   └── JovanPetsApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml
```

La carpeta `document/` puede contener los documentos MongoDB anotados
con `@Document`. Si se utiliza una estructura diferente, mantener
explícita la separación entre dominio y persistencia.

### Frontend

Estructura de referencia:

``` text
frontend/
└── src/
    ├── assets/
    ├── components/
    ├── pages/
    ├── layouts/
    ├── services/
    ├── hooks/
    ├── contexts/
    ├── interfaces/
    ├── routes/
    └── styles/
```

Mantener separadas las responsabilidades de presentación, comunicación
con la API y gestión del estado.

------------------------------------------------------------------------

## 7. Diseño de la interfaz

La interfaz debe transmitir una identidad visual relacionada con una
veterinaria y el cuidado de mascotas.

Características:

-   Diseño moderno, limpio y adaptable.
-   Navegación sencilla.
-   Formularios fáciles de utilizar.
-   Panel administrativo diferenciado de la vista del cliente.
-   Mensajes claros de confirmación y error.
-   Tablas y listados organizados.
-   Tarjetas para mostrar productos y servicios.
-   Consistencia en colores, tipografía, botones, formularios y
    componentes.

Crear una página de inicio que presente Jovan Pets, sus servicios
principales y accesos a las funcionalidades disponibles.

Tomar los wireframes existentes como referencia prioritaria. No
reemplazarlos por diseños distintos sin justificación y autorización.

No sobrecargar la interfaz con elementos innecesarios. Todos los
botones, enlaces, formularios y controles deben tener un comportamiento
funcional.

------------------------------------------------------------------------

## 8. Seguridad y validaciones

Implementar:

-   Autenticación de usuarios.
-   Autorización basada en roles y permisos.
-   Protección de rutas y endpoints.
-   Validación de formularios en frontend y backend.
-   Almacenamiento seguro de contraseñas mediante hashing.
-   Control de acceso a funcionalidades administrativas.
-   Validación de datos antes de persistirlos.
-   Manejo global de excepciones.
-   Protección de información personal.
-   Configuración segura de CORS según los orígenes autorizados.
-   Uso de variables de entorno para credenciales y secretos.

No almacenar contraseñas en texto plano. No incluir credenciales reales,
claves privadas ni secretos en el repositorio.

No permitir que un usuario acceda o modifique información privada de
otros usuarios sin autorización.

------------------------------------------------------------------------

## 9. Reglas de negocio y consistencia de datos

Implementar las reglas que correspondan a las historias de usuario y
documentarlas.

Ejemplos iniciales:

-   No permitir citas en horarios ocupados.
-   No permitir pedidos de productos sin existencias suficientes.
-   Actualizar el inventario cuando se confirme una operación que
    implique salida de productos.
-   Asociar las mascotas con sus propietarios.
-   Verificar permisos antes de realizar operaciones.
-   Evitar pedidos sin productos.
-   Calcular los totales de los pedidos desde el backend.
-   Controlar los estados de citas y pedidos.
-   Evitar cantidades de inventario negativas.
-   Validar que las referencias a otros documentos existan y sean
    válidas.

Al diseñar MongoDB:

-   Elegir entre incrustar documentos o utilizar referencias según los
    patrones de consulta y actualización.
-   Crear índices para búsquedas frecuentes y restricciones de unicidad
    necesarias.
-   No asumir que MongoDB aplica automáticamente integridad referencial
    como una base de datos relacional.
-   Validar las referencias desde la lógica de aplicación cuando
    corresponda.
-   Utilizar operaciones atómicas o transacciones cuando el proceso
    requiera consistencia entre varios documentos y el despliegue de
    MongoDB lo soporte.
-   Documentar cualquier decisión sobre consistencia, concurrencia e
    índices.

No implementar reglas de negocio basándose únicamente en validaciones
visuales del frontend.

------------------------------------------------------------------------

## 10. API e integración

Diseñar endpoints REST claros y coherentes con los módulos del sistema.

-   Utilizar métodos HTTP según su propósito: GET, POST, PUT/PATCH y
    DELETE.
-   Mantener convenciones consistentes para nombres y rutas.
-   Utilizar códigos de estado HTTP adecuados.
-   Validar las solicitudes recibidas.
-   Definir DTOs de entrada y salida.
-   Documentar los endpoints principales.
-   Manejar errores con respuestas consistentes.
-   Configurar la URL del backend en el frontend mediante variables de
    entorno.

No simular respuestas exitosas cuando el backend no haya completado la
operación. No utilizar datos estáticos como sustituto de operaciones que
deben persistirse.

------------------------------------------------------------------------

## 11. Pruebas y funcionamiento

No considerar una funcionalidad terminada solamente porque aparece en
pantalla.

Cada módulo debe estar conectado con el backend y MongoDB.

Realizar pruebas de:

-   Registro e inicio de sesión.
-   Gestión de usuarios y permisos.
-   Registro y consulta de mascotas.
-   Creación y administración de citas.
-   Consulta y administración de productos.
-   Creación y consulta de pedidos.
-   Actualización de inventario.
-   Validaciones de datos.
-   Manejo de errores.
-   Protección de endpoints.
-   Persistencia y consulta de documentos MongoDB.

Implementar pruebas unitarias para la lógica de negocio y pruebas de
integración para los procesos principales. Utilizar una base de datos de
pruebas o una configuración aislada; nunca ejecutar pruebas destructivas
sobre datos reales.

Verificar que frontend, backend y MongoDB funcionen correctamente en
conjunto.

------------------------------------------------------------------------

## 12. Documentación y entregables

### Aplicación

-   Frontend funcional en React con TypeScript.
-   Backend funcional con Java y Spring Boot.
-   Base de datos MongoDB.
-   Integración completa entre frontend y backend.
-   Autenticación y autorización.
-   Módulos implementados según las historias de usuario.
-   Código organizado y documentado.
-   Pruebas de los procesos principales.

### Diagramas de arquitectura en Visual Paradigm

Entregar los siguientes cinco diagramas:

1.  **Vista de contexto:** actores externos y su interacción con Jovan
    Pets.
2.  **Vista funcional:** diagrama UML de componentes.
3.  **Vista lógica:** diagrama UML de clases del dominio.
4.  **Vista de desarrollo:** diagrama que represente la arquitectura por
    capas.
5.  **Vista de despliegue:** diagrama UML de despliegue que muestre
    frontend, backend, MongoDB y sus comunicaciones.

Los diagramas deben ser elaborados en Visual Paradigm y entregarse en
formato editable del proyecto cuando sea posible, además de exportarse a
PDF o imagen para su revisión.

No entregar únicamente imágenes generadas sin estructura UML editable.
Verificar que nombres, módulos, clases, responsabilidades y conexiones
coincidan con el código.

### Documentación técnica

Crear un `README.md` que incluya:

-   Descripción del proyecto.
-   Tecnologías utilizadas.
-   Requisitos de instalación.
-   Configuración de MongoDB.
-   Variables de entorno necesarias.
-   Instrucciones para ejecutar el backend.
-   Instrucciones para ejecutar el frontend.
-   Estructura del proyecto.
-   Funcionalidades implementadas.
-   Ejecución de pruebas.
-   Descripción breve de la arquitectura.

------------------------------------------------------------------------

## 13. Instrucciones de trabajo para la IA

### Antes de programar

1.  Analizar todos los archivos existentes del repositorio.
2.  Revisar historias de usuario y wireframes.
3.  Identificar requisitos funcionales y no funcionales.
4.  Identificar funcionalidades y actores reales.
5.  Definir módulos y responsabilidades.
6.  Diseñar el modelo lógico del dominio.
7.  Diseñar el modelo de documentos y colecciones MongoDB.
8.  Establecer la arquitectura por capas.
9.  Definir endpoints y contratos de API.
10. Crear una matriz de trazabilidad entre historias de usuario,
    pantallas, componentes, clases, endpoints, colecciones y pruebas.
11. Presentar un plan de implementación por fases antes de realizar
    cambios grandes.

### Durante el desarrollo

-   Trabajar de manera ordenada y progresiva.
-   Implementar módulos completos.
-   Mantener coherencia entre frontend, backend y MongoDB.
-   Respetar historias de usuario y wireframes.
-   No modificar requisitos existentes sin justificación.
-   Evitar código duplicado.
-   Aplicar buenas prácticas de programación.
-   Explicar decisiones técnicas importantes.
-   No dejar botones ni formularios sin funcionalidad.
-   No utilizar información ficticia como sustituto de funcionalidades
    reales.
-   No afirmar que una funcionalidad está terminada sin haberla probado.
-   Mantener actualizada la documentación.
-   No introducir PostgreSQL, MySQL, Hibernate o JPA en el proyecto.

### Al finalizar cada fase

-   Ejecutar las pruebas correspondientes.
-   Revisar errores de compilación y ejecución.
-   Verificar la integración con MongoDB.
-   Actualizar la matriz de trazabilidad.
-   Informar qué se implementó, qué se probó y qué está pendiente.
-   No avanzar ocultando errores o dejando funcionalidades incompletas.

------------------------------------------------------------------------

## 14. Prioridad de desarrollo

Seguir este orden, ajustándolo únicamente cuando exista una razón
técnica documentada.

### Fase 1. Análisis y planificación

-   Revisar historias de usuario.
-   Revisar wireframes.
-   Identificar requisitos y actores.
-   Definir módulos.
-   Establecer arquitectura por capas.
-   Diseñar el modelo lógico.
-   Diseñar documentos, colecciones e índices MongoDB.
-   Definir contratos de API.
-   Planificar pruebas y entregables.

### Fase 2. Configuración y backend

-   Configurar el proyecto Spring Boot.
-   Configurar conexión con MongoDB.
-   Configurar variables de entorno.
-   Implementar documentos y modelos.
-   Implementar repositories con Spring Data MongoDB.
-   Implementar services y reglas de negocio.
-   Crear controllers y endpoints REST.
-   Implementar autenticación y autorización.
-   Implementar manejo de excepciones.
-   Crear pruebas unitarias y de integración.

### Fase 3. Frontend

-   Crear o adaptar la estructura de React con TypeScript.
-   Implementar diseño basado en los wireframes.
-   Crear páginas, componentes y layouts.
-   Implementar formularios y validaciones.
-   Conectar los servicios con el backend.
-   Implementar estados de carga, éxito y error.
-   Implementar control de acceso según roles.

### Fase 4. Integración y pruebas

-   Verificar conexión con MongoDB.
-   Probar todos los módulos.
-   Verificar persistencia de información.
-   Probar reglas de negocio.
-   Probar permisos y seguridad.
-   Corregir errores.
-   Comprobar el funcionamiento completo de la aplicación.

### Fase 5. Diagramas y documentación

-   Elaborar los cinco diagramas en Visual Paradigm.
-   Verificar que coincidan con el código y los requisitos.
-   Documentar las vistas arquitectónicas.
-   Preparar README y documentación técnica.
-   Preparar los entregables finales.

------------------------------------------------------------------------

## 15. Criterios finales de aceptación

El proyecto Jovan Pets se considerará terminado cuando:

-   La aplicación web funcione correctamente.
-   El frontend esté conectado al backend.
-   El backend esté desarrollado con Java y Spring Boot.
-   MongoDB sea la base de datos utilizada por el sistema.
-   La persistencia se implemente con Spring Data MongoDB.
-   No existan dependencias de persistencia relacional no autorizadas.
-   Los módulos principales estén implementados según las historias de
    usuario.
-   Las funcionalidades respeten roles y permisos.
-   Las reglas de negocio estén implementadas en el backend.
-   Existan pruebas de funcionamiento.
-   Se hayan elaborado los cinco diagramas requeridos en Visual
    Paradigm.
-   Los diagramas coincidan con la implementación real.
-   La documentación permita instalar, configurar y ejecutar el
    proyecto.

**Objetivo final:** entregar un sistema web funcional, organizado,
seguro y documentado, que represente las necesidades reales de Jovan
Pets y cumpla con los requisitos académicos de arquitectura de software,
utilizando obligatoriamente Java, Spring Boot, React con TypeScript y
MongoDB.

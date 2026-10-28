# Jovan Pets — entrega de Ingeniería de Software

Análisis de las 24 HU, backend Java/Spring Boot, frontend, persistencia y tres vistas nativas de Visual Paradigm. Código revisado el **27 de octubre de 2026**. Diagrama de componentes actualizado el **28 de octubre de 2026**.

## Entregables principales

- [Análisis y decisiones](docs/ANALISIS.md)
- [Matriz de trazabilidad de las 24 HU](docs/TRAZABILIDAD.md)
- [Inventario y procedencia de los archivos](docs/INVENTARIO.md)
- [Textos originales de las HU de GitHub](docs/fuentes/HISTORIAS-ORIGINALES.md)
- [Justificación de los diagramas](docs/DIAGRAMAS.md)
- [Guía para la exposición](docs/GUIA-EXPOSICION.md)
- [Validación ejecutada y límites](docs/VALIDACION.md)
- [Wireframes originales recuperados](wireframes/index.html)

| Vista | Imagen exportada por Visual Paradigm |
|---|---|
| Contexto | [01-contexto.png](docs/diagramas/01-contexto.png) |
| Funcional, puertos e interfaces conectadas | [02-componentes.png](docs/diagramas/02-componentes.png) |
| Lógica, clases del dominio | [03-clases.png](docs/diagramas/03-clases.png) |

**Abrir y editar en Visual Paradigm:** [Jovan-Pets.vpp](docs/diagramas/Jovan-Pets.vpp). Contiene exactamente las tres vistas, con elementos nativos. Las exportaciones conservan la marca de agua de evaluación. [Registro de comprobación](docs/diagramas/validacion-modelo.txt).

## Ejecutar

Requisito: JDK 21. Maven Wrapper descarga Maven y las dependencias en su primer uso.

```powershell
cd backend
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Abra **http://localhost:8080**. Spring Boot sirve frontend y API bajo el mismo origen. El frontend está en `frontend/` y se incorpora al JAR al compilar. No abrir `index.html` con `file://`.

También puede ejecutar `java -jar target/jovan-pets-1.0.0.jar` desde `backend/`, después de compilar. En Linux/macOS use `sh mvnw clean verify` y `sh mvnw spring-boot:run`.

## Cuentas ficticias de demostración

Solo se crean en modo demostración, cuando no existen usuarios. Contraseña de las tres cuentas: `JovanDemo2026!`.

| Rol | Correo |
|---|---|
| Administrador | admin@jovanpets.test |
| Personal veterinario | personal@jovanpets.test |
| Cliente | cliente@jovanpets.test |

`APP_DEMO=true` habilita datos ficticios para la exposición local. El perfil PostgreSQL los deshabilita. `.env.example` documenta variables; Spring Boot no carga automáticamente ese archivo.

## Persistencia y arquitectura

La ejecución local guarda H2 en `backend/datos/jovanpets.mv.db` si se arranca desde `backend/`. Las pruebas usan otra base en memoria. El esquema PostgreSQL y su configuración están en `base-datos/`; requieren crear una base vacía, aplicar el SQL y establecer `DB_URL`, `DB_USUARIO`, `DB_CLAVE` y el perfil `postgres`. La conexión a un servidor PostgreSQL real no se probó en esta revisión.

```text
frontend/                             HTML, CSS, JavaScript
backend/src/main/java/com/jovanpets/
  controlador/                        API y contratos de entrada
  servicio/                           Casos de uso, permisos y transacciones
  repositorio/                        Spring Data JPA
  dominio/                            Entidades y enumeraciones
  configuracion/                      Seguridad y datos ficticios
base-datos/                           Esquema PostgreSQL
```

Se mantienen BCrypt con sal, CSRF, sesión de 30 minutos y validación de rol/propiedad en servidor. Los pedidos recalculan precios y bloquean existencias de forma transaccional.

## Estado real

El `main` inicial estaba vacío tras eliminarse documentos. Se recuperaron los requisitos y wireframes del historial y se reutilizó una entrega local anterior del mismo proyecto. Se corrigieron cancelación de citas pasadas y productos de precio cero, con pruebas nuevas.

**Diez pruebas de integración pasaron**, incluidas concurrencia de inventario y reservas. Se comprobó en navegador el recorrido de inicio de sesión → carrito → pedido → persistencia y decremento de stock, usando otra base local con datos ficticios.

Hay cobertura parcial y pendiente: recuperación por correo (HU-22), notificaciones (HU-10), auditoría de clientes, edición de antecedentes, paginación y filtro por veterinario, entre otros criterios descritos en la matriz. Peluquería, horarios y modalidades de entrega se conservan como decisiones del prototipo que requieren confirmación. No se procesan pagos electrónicos ni se realizó un nuevo despliegue público.

La revisión del 28 de octubre incorpora las conexiones de componentes solicitadas a partir de dos presentaciones de referencia. El proyecto completo conserva contexto y clases. También se incluye [un VPP independiente de componentes](docs/diagramas/Jovan-Pets-componentes.vpp).

# Guía breve para exponer Jovan Pets

1. **Problema.** La veterinaria familiar necesita centralizar los datos de clientes y mascotas, organizar citas y controlar pedidos e inventario. El objetivo es reducir registros manuales y mantener información consistente.
2. **Contexto.** Explicar el círculo central y los tres roles humanos. Las flechas son datos: solicitudes, información clínica y comercial, confirmaciones y consultas. El correo aparece como integración pendiente por HU-22.
3. **Responsabilidades.** Presentar el portal/API y sus cinco módulos funcionales. Seguir las líneas continuas desde los puertos hacia cada contrato. El círculo indica que el módulo provee el contrato y el semicírculo indica que el portal/API lo requiere. Son módulos de una misma aplicación Spring Boot. No se dibujan flechas UML Dependency entre cajas.
4. **Información del negocio.** Recorrer Cliente → Mascota → Cita/Atencion y Cliente → Pedido → DetallePedido → Producto. Explicar el rombo de composición y las multiplicidades. Usuario representa acceso; Cliente representa la ficha del propietario.
5. **Capas reales.** Frontend → ApiControlador → interfaz de servicio → implementación del servicio → repositorio JPA → base relacional. Las validaciones comerciales están en servicios; las transacciones protegen compras e inventario.
6. **Demostración.** Iniciar sesión como administrador, crear un cliente y mascota, consultar agenda; usar la cuenta cliente para solicitar cita o comprar; mostrar que las existencias y el resumen se actualizan. Los datos iniciales son ficticios.
7. **Evidencia.** Mostrar VALIDACION.md y validacion-modelo.txt. Diez pruebas de integración pasaron; incluyen permisos, CSRF, propiedad, compras y citas concurrentes y las dos correcciones de esta revisión.
8. **Límites.** No afirmar que todo está terminado. Recuperación por correo, notificaciones, auditoría de clientes y otros criterios siguen pendientes. La matriz indica exactamente cuáles. El perfil PostgreSQL está preparado, pero esta entrega se probó con H2.

## Respuestas a preguntas previsibles

**¿Por qué no hay una clase Inventario?** Las existencias pertenecen a Producto y los cambios se registran en MovimientoInventario. Es lo que representa el código actual; una clase vacía adicional duplicaría el concepto.

**¿Por qué no hay subclases Administrador y Cliente?** Los permisos cambian mediante el atributo rol de Usuario. La ficha Cliente es otro concepto y puede existir sin una cuenta.

**¿Por qué no hay métodos en las clases verdes?** Las entidades almacenan datos y relaciones; los casos de uso están implementados en servicios. El diagrama no inventa operaciones para imitar una imagen.

**¿Dónde están los pagos y el despliegue?** No se implementaron pagos electrónicos ni se efectuó un nuevo despliegue público en esta revisión. La prioridad acordada en AGENTS.md son análisis, trazabilidad y las tres primeras vistas.

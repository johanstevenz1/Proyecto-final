# Inventario de fuentes revisadas

## Estado de GitHub

- main al iniciar: 1d463339519c46c8f4267aee3b5901aab4e22956; arbol vacio.
- Ese commit elimino REQUERIMIENTOS.md. El anterior, 60d17d1, elimino wireframes/.
- Documentos recuperados para analisis desde 01c72eaf12bfff5bb9449f270ee91d20aab1ef35: REQUERIMIENTOS.md, 24 PNG, 11 HTML y el indice.
- 24 historias con texto y criterios leidas desde las incidencias #1 a #24. No se afirma que main contuviera codigo.
- Se reutilizo la entrega local anterior del 25 de octubre del mismo proyecto: Java 21, Spring Boot 3.5.16, JPA, Spring Security, H2/PostgreSQL y frontend HTML/CSS/JS. Se copio el codigo fuente y se revisaron entidades, controladores, servicios, configuracion y pruebas.
- La entrega anterior no habia consultado los textos de las incidencias; sus afirmaciones de cobertura se sustituyen por esta trazabilidad.

## HTML originales: contenido leido

### HU-01-registrar-cliente.html

HU-01: Registrar Cliente · WIREFRAME - HU-01 · 🐾 Jovan Pets · Dashboard · Clientes · Mascotas · Citas · Productos · Inicio · / · Clientes · / Registrar Cliente · Registrar Nuevo Cliente · Complete los datos del propietario de la mascota · Nombre Completo · * · Cédula · * · Debe ser única en el sistema · Teléfono · * · Email · * · Dirección · * · Cancelar · Guardar Cliente

### HU-02-editar-cliente.html

HU-02: Editar Cliente · WIREFRAME - HU-02 · 🐾 Jovan Pets · Dashboard · Clientes · Mascotas · Inicio · / · Clientes · / Editar Cliente · ✏️ · Editar Cliente · Modifique la información del cliente · Nombre Completo · Cédula · 🔒 Campo no editable · Teléfono · Email · Dirección · Cancelar · Guardar Cambios

### HU-03-buscar-clientes.html

HU-03: Buscar Clientes · WIREFRAME - HU-03 · 🐾 Jovan Pets · Dashboard · Clientes · Mascotas · Inicio · / Clientes / Buscar · 🔍 Buscar Clientes · Nombre · Cédula · Teléfono · Buscar · Resultados · 3 clientes encontrados · Nombre · Cédula · Teléfono · Email · Acciones · Juan Pérez García · 1234567890 · 310 123 4567 · juan@email.com · ✏️ Editar · 👁️ Ver · María López Ruiz · 0987654321 · 315 987 6543 · maria@email.com · ✏️ Editar · 👁️ Ver · Carlos Martínez · 1122334455 · 320 111 2233 · carlos@email.com · ✏️ Editar · 👁️ Ver

### HU-04-listado-clientes.html

HU-04: Listado de Clientes · WIREFRAME - HU-04 · 🐾 Jovan Pets · Dashboard · Clientes · Mascotas · Inicio · / Clientes · 📋 Listado de Clientes · + Nuevo Cliente · Nombre · Cédula · Teléfono · Email · Mascotas · Acciones · Juan Pérez García · 1234567890 · 310 123 4567 · juan@email.com · 2 · ✏️ · 🐕 · 🗑️ · María López Ruiz · 0987654321 · 315 987 6543 · maria@email.com · 1 · ✏️ · 🐕 · 🗑️ · Carlos Martínez · 1122334455 · 320 111 2233 · carlos@email.com · 3 · ✏️ · 🐕 · 🗑️ · Ana Rodríguez · 5566778899 · 301 555 6677 · ana@email.com · 1 · ✏️ · 🐕 · 🗑️ · Pedro Sánchez · 4433221100 · 318 444 3322 · pedro@email.com · 0 · ✏️ · 🐕 · 🗑️ · ← Anterior · 1 · 2 · 3 · Siguiente →

### HU-05-registrar-mascota.html

HU-05: Registrar Mascota · WIREFRAME - HU-05 · Jovan Pets · Inicio · / Mascotas / Registrar Mascota · 🐕 · Registrar Nueva Mascota · JP · Juan Pérez García · Cédula: 1234567890 · Nombre · * · Especie · * · Perro · Gato · Ave · Otro · Raza · * · Sexo · * · Macho · Hembra · Fecha de Nacimiento · Peso (kg) · * · Cancelar · Registrar Mascota

### HU-06-antecedentes.html

HU-06: Antecedentes Medicos · WIREFRAME - HU-06 · Jovan Pets · Inicio · / Mascotas / Max / Antecedentes · 🐕 · Max · Propietario: Juan Pérez García | Labrador • Macho · Antecedentes Medicos · + Nuevo Antecedente · 15 Ago 2026 · Consulta General · Diagnostico: · Revision de rutina - Sin novedad · Se recomienda mantener dieta balanceada. · 01 Jul 2026 · Vacunacion · Aplicacion: · Rabia - Dosis anual · 20 Jun 2026 · Desparasitacion · Producto: · Drontal Plus - Dosis unica

### HU-07-historial-controles.html

HU-07: Historial de Controles · WIREFRAME - HU-07 · Jovan Pets · Inicio · / Mascotas / Max / Historial · 🐕 · Max · Propietario: Juan Pérez García · Desde · Hasta · Filtrar · Fecha · Tipo · Descripcion · Veterinario · 15 Ago 2026 · Consulta · Revision de rutina - Sin novedad · Dr. Martinez · 01 Jul 2026 · Vacuna · Rabia - Dosis anual · Dr. Martinez · 20 Jun 2026 · Desparasitacion · Drontal Plus - Dosis unica · Dr. Lopez · 15 Mar 2026 · Cirugia · Esterilizacion - Recuperacion exitosa · Dr. Lopez

### HU-08-vacunas.html

HU-08: Vacunas y Desparasitaciones · WIREFRAME - HU-08 · Jovan Pets · Inicio · / Mascotas / Max / Vacunas · 🐕 · Max · Propietario: Juan Pérez García · Vacunas · Desparasitaciones · Vacunas Registradas · + Nueva Vacuna · Rabia · Aplicada: 01 Jul 2026 · Proxima: 01 Jul 2027 · Parvovirus · Aplicada: 15 Mar 2026 · Proxima: 15 Sep 2026 (vence pronto) · Moquillo · Aplicada: 20 Ene 2026 · Proxima: 20 Ene 2027 · Leptospirosis · Aplicada: 10 Nov 2025 · Proxima: 10 May 2026 (vencida)

### HU-09-solicitar-cita.html

HU-09: Solicitar Cita · WIREFRAME - HU-09 · Jovan Pets · Inicio · / Citas / Solicitar Cita · Solicitar Nueva Cita · Seleccione el servicio, fecha y hora para su mascota · Mascota · * · Max (Labrador) · Luna (Gato) · Servicio · * · Consulta General · Vacunacion · Desparasitacion · Cirugia · Peluqueria · Fecha · * · Horas Disponibles · * · 8:00 · 8:30 · 9:00 · 9:30 · 10:00 · 10:30 · 11:00 · 11:30 · 14:00 · 14:30 · 15:00 · 15:30 · * Los horarios en gris ya estan ocupados · Cancelar · Confirmar Cita

### HU-10-confirmar-cita.html

HU-10: Confirmar o Rechazar Cita · WIREFRAME - HU-10 · Jovan Pets · Inicio · / Citas / Gestionar Citas · Gestionar Citas Pendientes · 3 citas pendientes de confirmar · Fecha · Hora · Cliente · Mascota · Servicio · Estado · Acciones · 05 Sep 2026 · 09:30 · Juan Perez · Max · Consulta General · Pendiente · Confirmar · Rechazar · 05 Sep 2026 · 10:00 · Maria Lopez · Luna · Vacunacion · Pendiente · Confirmar · Rechazar · 06 Sep 2026 · 14:00 · Carlos Martinez · Toby · Desparasitacion · Pendiente · Confirmar · Rechazar

### HU-11-calendario-citas.html

HU-11: Calendario de Citas · WIREFRAME - HU-11 · Jovan Pets · Inicio · / Citas / Calendario · Calendario de Citas - Semana · Semana Anterior · 1 - 7 Sep 2026 · Semana Siguiente · Hora · Lun 1 · Mar 2 · Mie 3 · Jue 4 · Vie 5 · Sab 6 · Dom 7 · 08:00 · Max - Consulta · 09:00 · Luna - Vacuna · Toby - Desparasitacion · 10:00 · Rex - Cirugia · Nina - Peluqueria · 14:00 · Max - Control · Confirmada · Pendiente · Cancelada

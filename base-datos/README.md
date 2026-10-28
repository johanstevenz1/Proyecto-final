# Persistencia de Jovan Pets

La demostración utiliza **H2 en archivo**, no almacenamiento temporal del navegador. Se crea automáticamente `backend/datos/jovanpets.mv.db`. Las pruebas automatizadas utilizan otra BD H2 en memoria y no alteran la demostración.

Se incluye un perfil PostgreSQL. Antes de usarlo, cree la BD y aplique `esquema-postgresql.sql`; configure `DB_URL`, `DB_USUARIO` y `DB_CLAVE`. Hibernate usa `validate`, por lo que no cambia el esquema de producción. El archivo SQL es una base inicial, no un sistema de migraciones versionadas.

La ejecución contra un servidor PostgreSQL real debe verificarse antes de publicar. Los respaldos automáticos, recuperación y migraciones con versionado quedan pendientes. No copie contraseñas de producción al repositorio.

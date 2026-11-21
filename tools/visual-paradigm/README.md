# Generador de modelos nativos

`EntregaDiagramas.java` implementa VPPlugin y VPPluginCommandLineSupport. Se compila con JDK y el `lib/openapi.jar` de Visual Paradigm. La carpeta del complemento debe contener `plugin.xml`, `classes/EntregaDiagramas.class` y un `salida.properties` con `salida=C:/ruta/de/exportacion`. No se incluye una ruta personal fija en los archivos versionados.

La clase principal se invoca desde Visual Paradigm mediante el comando documentado `com.vp.cmd.Plugin -project PROYECTO_BASE.vpp -pluginid com.jovanpets.entrega20261027`. El proyecto base debe ser una copia vacia de trabajo: el generador crea un proyecto nuevo. Se necesita una instalacion con licencia valida.

Para Windows donde el acceso CLI al registro no esta disponible, `PreferenciasMemoria.java` proporciona preferencias efimeras para esa ejecucion. Se compila y agrega al classpath con `-Djava.util.prefs.PreferencesFactory=PreferenciasMemoria`. No modifica la licencia ni las preferencias del usuario.

No se distribuyen JAR propietarios. El motor de Visual Paradigm guarda el `.vpp`, lo reabre, exporta los PNG y escribe `validacion-modelo.txt` con el tipo de cada diagrama y los conteos. La generacion falla si se encuentran relaciones Dependency o conectores en la vista funcional.

## Revision de componentes del 28 de octubre

`ComponentesConectados.java` extiende el generador inicial. Compilar ambas clases contra openapi.jar, elegir plugin-componentes.xml como plugin.xml y colocar las clases compiladas en classes/. En salida.properties se indican `entrada=C:/ruta/proyecto-base.vpp` y `salida=C:/ruta/exportacion`. Trabajar sobre una copia del proyecto base.

La revision sustituye solo el diagrama 02 y conserva los otros dos. Genera Jovan-Pets.vpp con las tres vistas, Jovan-Pets-componentes.vpp independiente, el PNG y el registro de validacion. Reabre ambos VPP y comprueba seis componentes, diez puertos, cinco interfaces y diez relaciones Realization/Usage hacia esas interfaces. No crea relaciones Dependency directamente entre cajas de componentes.

## Vistas 04 y 05 (21 de noviembre)

`VistasDesarrolloDespliegue.java` extiende `EntregaDiagramas` y agrega la vista de desarrollo (clases por capas) y la vista de despliegue al proyecto que ya tiene las vistas 01 a 03.

1. Compila para Java 11, que es el JRE de Visual Paradigm:
   `javac --release 11 -cp "C:\Program Files\Visual Paradigm 18.1\lib\openapi.jar" -d classes EntregaDiagramas.java PreferenciasMemoria.java VistasDesarrolloDespliegue.java`
2. Crea `%APPDATA%\VisualParadigm\plugins\com.jovanpets.vistas20261121\` con `plugin-vistas.xml` (renombrado a `plugin.xml`), la carpeta `classes\` y un `salida.properties` con `entrada=` (copia del proyecto base) y `salida=` (carpeta de exportación).
3. Desde `C:\Program Files\Visual Paradigm 18.1\bin`, ejecuta la clase `com.vp.cmd.Plugin` con el mismo classpath de `scripts\Plugin.bat` más la carpeta `classes`, y con `-Djava.awt.headless=true -Djava.util.prefs.PreferencesFactory=PreferenciasMemoria -project <base.vpp> -pluginid com.jovanpets.vistas20261121`.

El resultado es `Jovan-Pets.vpp` con las cinco vistas, sus cinco PNG y `validacion-modelo.txt`.

## Correcciones del profesor (21 de noviembre)

`VistasCorregidas.java` extiende `VistasDesarrolloDespliegue` y regenera las cinco vistas sobre el proyecto abierto con `-project`: contexto con flujos analizados por entidad, componentes conectados parte a parte, lógica desde el código y despliegue en Vercel, Render y Atlas.

1. Generar el modelo lógico: `node extraer-modelo.js ../../backend/src/main/java/com/jovanpets modelo-logico.txt`.
2. Compilar para Java 11: `javac --release 11 -cp "C:\Program Files\Visual Paradigm 18.1\lib\openapi.jar" -d classes EntregaDiagramas.java PreferenciasMemoria.java VistasDesarrolloDespliegue.java VistasCorregidas.java`.
3. Instalar como complemento `com.jovanpets.correccion20261121` con `plugin-correccion.xml` (renombrado a `plugin.xml`), la carpeta `classes\` y un `salida.properties` con `salida=` y `modelo=` (ruta de `modelo-logico.txt`).
4. Ejecutar `com.vp.cmd.Plugin` como en la sección anterior, con `-project` apuntando a una copia de `docs/diagramas/Jovan-Pets.vpp` y `-pluginid com.jovanpets.correccion20261121`.

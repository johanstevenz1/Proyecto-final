# Generador de modelos nativos

`EntregaDiagramas.java` implementa VPPlugin y VPPluginCommandLineSupport. Se compila con JDK y el `lib/openapi.jar` de Visual Paradigm. La carpeta del complemento debe contener `plugin.xml`, `classes/EntregaDiagramas.class` y un `salida.properties` con `salida=C:/ruta/de/exportacion`. No se incluye una ruta personal fija en los archivos versionados.

La clase principal se invoca desde Visual Paradigm mediante el comando documentado `com.vp.cmd.Plugin -project PROYECTO_BASE.vpp -pluginid com.jovanpets.entrega20261027`. El proyecto base debe ser una copia vacia de trabajo: el generador crea un proyecto nuevo. Se necesita una instalacion con licencia valida.

Para Windows donde el acceso CLI al registro no esta disponible, `PreferenciasMemoria.java` proporciona preferencias efimeras para esa ejecucion. Se compila y agrega al classpath con `-Djava.util.prefs.PreferencesFactory=PreferenciasMemoria`. No modifica la licencia ni las preferencias del usuario.

No se distribuyen JAR propietarios. El motor de Visual Paradigm guarda el `.vpp`, lo reabre, exporta los PNG y escribe `validacion-modelo.txt` con el tipo de cada diagrama y los conteos. La generacion falla si se encuentran relaciones Dependency o conectores en la vista funcional.

## Revision de componentes del 28 de octubre

`ComponentesConectados.java` extiende el generador inicial. Compilar ambas clases contra openapi.jar, elegir plugin-componentes.xml como plugin.xml y colocar las clases compiladas en classes/. En salida.properties se indican `entrada=C:/ruta/proyecto-base.vpp` y `salida=C:/ruta/exportacion`. Trabajar sobre una copia del proyecto base.

La revision sustituye solo el diagrama 02 y conserva los otros dos. Genera Jovan-Pets.vpp con las tres vistas, Jovan-Pets-componentes.vpp independiente, el PNG y el registro de validacion. Reabre ambos VPP y comprueba seis componentes, diez puertos, cinco interfaces y diez relaciones Realization/Usage hacia esas interfaces. No crea relaciones Dependency directamente entre cajas de componentes.

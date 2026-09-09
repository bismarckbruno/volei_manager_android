# Política de Privacidad

**Última actualización:** Septiembre de 2026

**Voleicito** es una aplicación Android gratuita y de código abierto para organizar partidos recreativos de vóley. Esta política describe cómo se tratan los datos en la aplicación y cómo podrían operar funciones futuras opcionales.

El proyecto se distribuye bajo la **GNU General Public License v3.0 (GPL-3.0)**.

---

### 1. Datos Recopilados
La aplicación almacena solo los datos que usted proporciona manualmente en la base de datos local, entre ellos:
* Nombres de jugadores;
* Grupos y configuraciones del grupo;
* Historial de partidos;
* Puntuación Elo y metadatos relacionados con el partido.

Por defecto, la app no recopila identificadores personales, identificadores de publicidad, ubicación, datos de contacto ni comportamiento de navegación. No se recopilan automáticamente datos personales sensibles.

### 2. Telemetría Anónima (Análisis de Uso e Informes de Fallos)
La app incluye una función opcional y anónima de telemetría, con **Firebase Analytics** y **Firebase Crashlytics** (Google). Solo se habilita después de que usted acepte explícitamente mediante un diálogo claro mostrado en el primer uso de la app (o luego, desde el menú de configuración). El objetivo de esta telemetría es comprender el uso de la app, la estabilidad y el rendimiento, no perfilar a personas.

Ningún dato se recopila antes de su consentimiento: la recolección de telemetría está desactivada por defecto en la app y solo se activa en respuesta a su consentimiento explícito.

Cuando está habilitada, la telemetría puede incluir información agregada, como:
* Versión de la app;
* Versión de Android;
* Información básica de plataforma/compatibilidad;
* Datos anónimos de fallos y errores (mediante Firebase Crashlytics);
* Eventos de uso de funciones (por ejemplo, "grupo creado", "partido finalizado", "equipos rebalanceados", "rebalanceo por racha de victorias", "backup/CSV exportado o importado"), sin identificadores personales ni del grupo.

Estos datos no incluyen nombres de jugadores, detalles de partidos, nombres de grupos, contactos ni contenido bruto ingresado en la app. No se usan para identificarle personalmente. Puede revocar el consentimiento en cualquier momento desde el menú de configuración de la app (el mismo interruptor usado para activarla), y la recolección de telemetría se detiene de inmediato al retirar el consentimiento.

### 3. Almacenamiento Local
Todos los datos principales de la app se almacenan localmente en su dispositivo mediante la base de datos interna (**Room/SQLite**). No se envían datos rutinarios de uso a servidores externos como parte de la funcionalidad base.

### 4. Funcionalidad Premium de Sincronización Futura
Podría introducirse en el futuro una función premium de sincronización que permita al usuario sincronizar datos de grupo, incluidos los jugadores, entre dispositivos mediante una cuenta de usuario. Esa funcionalidad puede requerir:
* Registro e inicio de sesión del usuario;
* Acceso mediante los proveedores admitidos;
* Aceptación explícita de los términos y del flujo de consentimiento, incluyendo reconocimiento/firma electrónica cuando corresponda;
* Almacenamiento y sincronización con **Firebase/Firestore**.

Cuando esté disponible, los datos de la cuenta pueden incluir información de identidad necesaria para la autenticación y el control de acceso, además de los datos sincronizados del grupo, como jugadores, grupos, configuraciones y metadatos relacionados. El uso de esta funcionalidad premium será opcional y estará separado de la experiencia principal de la app.
### 5. Compartir Datos
La aplicación no vende sus datos ni comparte sus datos locales con terceros como parte del producto base.

Para cualquier funcionalidad futura opcional de telemetría o sincronización premium, el intercambio se limitará a lo necesario para prestar ese servicio y seguirá el consentimiento y las divulgaciones definidas en el aviso correspondiente dentro de la app.

### 6. Servicios de Terceros
Si opta por la función opcional de telemetría anónima descrita en la sección 2, la app utiliza **Firebase Analytics** y **Firebase Crashlytics** (Google) para recopilar esos datos anónimos y agregados. La app no utiliza ningún otro servicio de analítica, red publicitaria ni seguimiento de comportamiento. Si en el futuro se introduce una sincronización premium, los proveedores implicados (por ejemplo, Firebase/Firestore) se usarán solo para prestar esa funcionalidad concreta.

### 7. Permisos
La app solicita el permiso de INTERNET, usado únicamente para enviar la telemetría anónima descrita en la sección 2 cuando usted da su consentimiento (no se envía ningún dato mientras la telemetría esté desactivada). La app no solicita otros permisos especiales del dispositivo, como cámara, micrófono o ubicación. Si en el futuro las funciones en la nube requieren permisos o servicios adicionales, se solicitarán solo cuando sean necesarios y se explicarán claramente al usuario.

### 8. Eliminación de Datos
Usted controla sus datos locales:
* Puede eliminar jugadores, grupos o historial de partidos directamente en la app.
* Al desinstalar la aplicación, se eliminan permanentemente todos los datos almacenados localmente.

Si se utiliza la funcionalidad futura de sincronización premium, también puede ser posible eliminar o desconectar los datos sincronizados de la cuenta desde la configuración de la cuenta o el panel de sincronización en la nube, según las funciones de gestión de cuentas del servicio.

### 9. Niños
La aplicación no está dirigida a menores de 13 años y no recopila intencionalmente datos personales de niños.

### 10. Cambios
Esta política puede actualizarse en futuras versiones para reflejar cambios en la app o requisitos legales. Los cambios significativos se comunicarán en las notas de la versión y en avisos dentro de la propia app cuando sea aplicable.

### 11. Contacto
Las preguntas sobre esta política pueden enviarse al desarrollador a través de la página oficial de la aplicación en **Google Play Store** o mediante el repositorio de GitHub asociado al proyecto.

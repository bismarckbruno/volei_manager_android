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

### 2. Telemetría Anónima Opcional (Funcionalidad Futura)
Podría añadirse una función opcional de telemetría anónima en una versión futura. Si se implementa, solo se habilitará después de que usted acepte explícitamente mediante una pantalla de consentimiento clara dentro de la app. El objetivo de esta telemetría es comprender el uso de la app, la estabilidad y el rendimiento, no perfilar a personas.

Si está habilitada, la telemetría puede incluir información agregada, como:
* Versión de la app;
* Versión de Android;
* Información básica de plataforma/compatibilidad;
* Datos anónimos de fallos y errores;
* Eventos de uso de funciones (por ejemplo, "sorteo ejecutado" o "partido finalizado"), sin identificadores personales ni del grupo.

Estos datos no incluirán nombres de jugadores, detalles de partidos, nombres de grupos, contactos ni contenido bruto ingresado en la app. No se usarán para identificarle personalmente. Puede revocar el consentimiento en cualquier momento desde la configuración de la app, y la recolección de telemetría se detendrá cuando se retire el consentimiento.

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
La app no utiliza actualmente servicios de analítica, redes publicitarias ni seguimiento de comportamiento. Si en el futuro se activa telemetría opcional o sincronización en la nube, los proveedores implicados (por ejemplo, Firebase/Firestore) se usarán solo para prestar esa funcionalidad concreta.

### 7. Permisos
La app no solicita permisos especiales del dispositivo, como cámara, micrófono o ubicación. Si en el futuro las funciones en la nube requieren permisos o servicios adicionales, se solicitarán solo cuando sean necesarios y se explicarán claramente al usuario.

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

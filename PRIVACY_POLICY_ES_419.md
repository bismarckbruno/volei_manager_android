# Política de Privacidad

**Última actualización:** Septiembre de 2026

**Voleicito** es una aplicación Android gratuita y de código abierto para organizar partidos recreativos de vóley. Esta política describe cómo se tratan los datos en la aplicación, incluyendo las funciones opcionales de inicio de sesión, sincronización premium en la nube, transmisión en vivo para espectadores y telemetría.

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

### 4. Inicio de Sesión, Sincronización Premium en la Nube y Transmisión en Vivo para Espectadores
La app ofrece inicio de sesión opcional y funciones premium que van más allá de la experiencia solo local descrita arriba:
* **Inicio de sesión**: puede crear una cuenta gratuita (correo/contraseña o inicio de sesión con Google) o, en el caso de los espectadores, unirse a un grupo de forma anónima con un código de invitación, todo mediante **Firebase Authentication**.
* **Sincronización premium en la nube**: una suscripción paga permite que el organizador del grupo sincronice los datos del grupo —jugadores, marcador en vivo, historial de partidos y registros de Elo— entre dispositivos, usando **Firebase/Firestore**.
* **Transmisión en vivo para espectadores**: el organizador puede compartir un código de invitación/acceso para que los espectadores sigan el marcador en tiempo real y, si el organizador lo habilita, también el historial de partidos y el Elo de los jugadores, sin que el espectador necesite crear una cuenta completa.
* **Personalización de colores de equipo**: los suscriptores pueden personalizar los colores de cada equipo en su propio dispositivo; el organizador de un grupo sincronizado también puede definir colores que se apliquen a todos los que visualicen ese grupo.

Estas funciones son opcionales y están separadas de la experiencia principal de la app, que es local. Los datos de la cuenta pueden incluir su correo electrónico, nombre visible y un identificador único de cuenta usado para la autenticación y el control de acceso, además de los datos de grupo sincronizados descritos arriba.

### 5. Suscripciones y Pagos
Las suscripciones Premium (planes mensual y anual) se compran y gestionan mediante **Google Play Billing**. Los precios los define y muestra Google Play al momento de la compra y pueden variar según el país/región e impuestos aplicables. Los datos de pago (como la información de la tarjeta) son procesados enteramente por Google y nunca son recopilados ni almacenados por la app ni por el desarrollador. Las suscripciones se renuevan automáticamente hasta que se cancelan y pueden revisarse, cambiarse o cancelarse en cualquier momento desde la página de suscripciones de Google Play Store. Los reembolsos siguen las políticas propias de Google Play.

### 6. Compartir Datos
La aplicación no vende sus datos ni comparte sus datos locales con terceros como parte del producto base.

Si usa la sincronización en la nube o la transmisión en vivo para espectadores, los datos de grupo correspondientes (jugadores, marcador en vivo, historial de partidos, registros de Elo) se almacenan en Firebase/Firestore y están disponibles solo para los dispositivos/cuentas a los que usted otorgue acceso explícitamente: miembros del grupo y espectadores con un código de invitación válido. Los datos de suscripción y pago los gestiona Google Play Billing conforme a la política de privacidad propia de Google, y la telemetría (Sección 2) se comparte solo como se describe allí.

### 7. Servicios de Terceros
La app utiliza **Firebase Authentication**, **Firebase Firestore** y **Firebase Cloud Functions** (Google) para habilitar el inicio de sesión, la sincronización premium en la nube y la transmisión en vivo para espectadores, además de **Google Play Billing** (Google) para procesar las compras de suscripción. Si opta por la función opcional de telemetría anónima descrita en la Sección 2, la app también utiliza **Firebase Analytics** y **Firebase Crashlytics**. La app no utiliza ningún otro servicio de analítica, red publicitaria ni seguimiento de comportamiento.

### 8. Permisos
La app solicita el permiso de INTERNET, usado para sincronizar datos con Firebase cuando utiliza las funciones opcionales de inicio de sesión, sincronización premium en la nube o transmisión en vivo, y para enviar la telemetría anónima descrita en la Sección 2 cuando usted da su consentimiento (no se realiza ninguna solicitud de red para estos fines si no utiliza esas funciones). La app no solicita otros permisos especiales del dispositivo, como cámara, micrófono o ubicación.

### 9. Eliminación de Datos
Usted controla sus datos locales:
* Puede eliminar jugadores, grupos o historial de partidos directamente en la app.
* Al desinstalar la aplicación, se eliminan permanentemente todos los datos almacenados localmente.

Si se usa la sincronización premium en la nube, eliminar un grupo sincronizado en la app también borra los datos de ese grupo en la nube (jugadores, marcador en vivo, historial de partidos, registros de Elo, registros de miembros/espectadores y códigos de invitación) de nuestros servidores; esta acción no se puede deshacer. Eliminar su cuenta también borra el registro de su cuenta y todos los grupos en la nube de los que sea propietario, incluso si la app no pudo confirmar la eliminación antes de que se eliminara la cuenta. Cancelar una suscripción detiene los cobros futuros, pero por sí solo no elimina sus datos en la nube; use la eliminación de grupo o de cuenta dentro de la app para eso.

### 10. Niños
La aplicación no está dirigida a menores de 13 años y no recopila intencionalmente datos personales de niños.

### 11. Cambios
Esta política puede actualizarse en futuras versiones para reflejar cambios en la app o requisitos legales. Los cambios significativos se comunicarán en las notas de la versión y en avisos dentro de la propia app cuando sea aplicable.

### 12. Contacto
Las preguntas sobre esta política pueden enviarse al desarrollador a través de la página oficial de la aplicación en **Google Play Store** o mediante el repositorio de GitHub asociado al proyecto.

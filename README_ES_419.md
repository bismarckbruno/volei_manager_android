# Vóley Manager 🏐

**Vóley Manager** es una aplicación Android desarrollada con **Jetpack Compose** y **Room Database** para gestionar partidos recreativos de vóley, automatizar el sorteo de equipos, seguir el rendimiento de los jugadores (ranking Elo) y garantizar una rotación justa.

## ✨ Funcionalidades

### 🎮 Gestión de Partidos
- **Sorteo Automático Inteligente**: La app selecciona a las personas y equilibra los equipos de la forma más justa posible, mezclando niveles de habilidad (usando Elo) y distribuyendo de forma uniforme a los jugadores prioritarios.
- **Rotación Justa**: Sistema de cola inteligente para asegurar que todos jueguen.
  - **Prioridad por Partidos**: Al decidir quién entra a la cancha o quién se mantiene para jugar más, **la app prioriza a quien jugó menos veces**.
  - **Manejo de Racha por Modo**: Al alcanzar el límite de victorias, la app aplica el modo elegido: en **Rebalanceo**, divide al equipo ganador; en **Descanso**, puede sacar a los ganadores para dar turno a la cola ("Rey de la Cancha").
- **Armado Manual**: Pantalla dedicada para seleccionar o ajustar manualmente la composición de los equipos.
- **Marcador en Tiempo Real**: Conteo de puntos durante el partido.

### 📊 Ranking y Estadísticas
- **Sistema Elo**: Puntaje dinámico calculado después de cada partido según la fuerza del rival (K=32, Elo inicial 1200).
- **Historial de Partidos**: Registro completo de juegos con equipos, marcador final, variación de Elo y promedios por equipo.
- **Compartir**: Exporta el historial como imagen para compartir en redes sociales.

### 👥 Gestión de Jugadores y Grupos
- **Múltiples Grupos**: Crea y administra grupos independientes (por ejemplo, "Vóley del Martes", "Vóley de Playa"), cada uno con sus propios jugadores, historial y configuraciones.
- **Perfil de Jugador**: Nombre, Elo y marca de prioridad.
- **Jugador Prioritario (`isPriority`)**: Marca genérica de equilibrio que puede representar armadores, equilibrio de género o cualquier criterio definido por el grupo.
- **Peaje por Llegada Tardía**: Quienes llegan tarde reciben partidos virtuales extra para compensar, calculados según el promedio de partidos ya jugados.
- **Copia y Restauración**: Exportación/importación de datos completos (JSON) o tablas específicas (CSV).

### 🎨 Personalización
- **Tema**: Claro, Oscuro o Sistema.
- **Visualización opcional**: Activa o desactiva Elo y peaje en la interfaz.

## 🛠 Tecnologías Utilizadas
- **Lenguaje**: Kotlin
- **Interfaz (UI)**: Jetpack Compose (Material Design 3)
- **Arquitectura**: MVVM — toda la lógica de negocio en `VoleiViewModel`; DI manual vía `ViewModelFactory`, sin Hilt/Dagger
- **Navegación**: Enum personalizado (`Screen.GAME`, `HISTORY`, `FAQ`, `ABOUT`) con `AnimatedContent`
- **Base de Datos Local**: Room (SQLite) con migraciones incrementales
- **Asincronismo**: Coroutines & Flow (`viewModelScope`, `Dispatchers.IO`)
- **Serialización JSON**: Gson 2.10.1
- **Procesamiento de Anotaciones**: KSP (Kotlin Symbol Processing)

## 🚀 Cómo Ejecutar el Proyecto
1. Clona el repositorio:
   ```bash
   git clone https://github.com/bismarckbruno/volei_manager_android.git
   ```
2. Abre el proyecto en **Android Studio**.
3. Sincroniza Gradle y ejecuta la app en un emulador o dispositivo físico (Android 7.0+ / API 24+).
4. Niveles de SDK actuales para publicación en Play Store: **compileSdk 36** y **targetSdk 36** (Android 16).

## ⚙️ Reglas Configurables por Grupo
- **Tamaño del Equipo**: De 2 a 6 jugadores por lado.
- **Límite de Victorias**: Máximo de victorias consecutivas antes de aplicar la regla del modo activo (dividir en Rebalanceo o rotar descanso en modo Descanso).
- **Prioridad Activada**: Garantiza al menos un jugador prioritario por equipo en el sorteo automático (si hay disponibilidad).

## 🤝 Contribución y Feedback
¡Las contribuciones son bienvenidas! Siéntete libre de abrir un *pull request*.

¿Encontraste un problema o tienes una idea? Abre un [Issue aquí](https://github.com/bismarckbruno/volei_manager_android/issues/new/choose).

## ⚖️ Documentación Legal
- [Política de Privacidad (ES-419)](https://bismarckbruno.github.io/volei_manager_android/PRIVACY_POLICY_ES_419)
- [Términos de Uso (ES-419)](https://bismarckbruno.github.io/volei_manager_android/TERMS_OF_USE_ES_419)
- [Privacy Policy (EN-US)](https://bismarckbruno.github.io/volei_manager_android/PRIVACY_POLICY)
- [Terms of Use (EN-US)](https://bismarckbruno.github.io/volei_manager_android/TERMS_OF_USE)
- [Política de Privacidade (PT-BR)](https://bismarckbruno.github.io/volei_manager_android/PRIVACY_POLICY_PT_BR)
- [Termos de Uso (PT-BR)](https://bismarckbruno.github.io/volei_manager_android/TERMS_OF_USE_PT_BR)
- [Licencia MIT](LICENSE)

## ☕ Apoya el Proyecto

**Vóley Manager** es un proyecto independiente y gratuito. Si la app te ayudó a organizar mejor tus partidos y quieres apoyar nuevas funcionalidades, puedes invitarme un café.

### Formas de apoyar:

* **GitHub Sponsors:** [Apoyar vía GitHub](https://github.com/sponsors/bismarckbruno)
* **PIX:** Opciones abajo:

<details>
  <summary><b>Haz clic para ver el código QR y la clave PIX</b></summary>
  <br>
  <div align="center">
    <img src="apoio/qr_code_pix.png" width="200" alt="Código QR PIX"><br>
    <sub>Escanea el código QR de arriba o usa el código de copia y pega de abajo:</sub>
    <br><br>
    <p><code>00020126650014br.gov.bcb.pix0136d143999e-2f7a-4ce4-84c3-b3b03b41536e0203Pix5204000053039865802BR5925BRUNO_BISMARCK_DA_SILVA_M6006CAXIAS62210517ApoioVoleiManager63044F13</code></p>
  </div>
</details>

---
*¡Cualquier aporte ayuda a mantener el café (y el código) fluyendo!* 🏐

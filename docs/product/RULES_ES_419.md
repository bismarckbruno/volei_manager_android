# Reglas de Negocio y Algoritmos

Este documento conecta reglas del producto, comportamiento actual y dolores del usuario.

## Matriz de Reglas

| Funcionalidad / Regla | Comportamiento Actual | Dolor del Usuario que Resuelve |
| --- | --- | --- |
| **Balanceo automático de equipos** | Arma equipos buscando paridad competitiva con promedio Elo y distribución de prioridad. | Quejas por grupitos y partidos desbalanceados. |
| **Marca de prioridad (`isPriority`)** | Garantiza al menos un jugador prioritario por equipo (si está disponible). | Desbalance en rol/atributo (armador, balance de género, etc.). |
| **Peaje por llegada tardía (`dailyToll`)** | Si un jugador llega después del 1er partido, recibe peaje diario igual al promedio de partidos ya jugados ese día. | Percepción de injusticia por llegadas tarde que "se meten" en la fila. |
| **Límite de victorias seguidas** | Entero configurable de 1 a 6 (valor frecuente 3; 2 y 3 son comunes). | Un solo equipo dominando la cancha demasiado tiempo. |
| **Manejo de racha: modo Rebalanceo** | Al llegar al límite, el equipo ganador se divide para la siguiente ronda. | Rachas largas con poca renovación de equipos. |
| **Manejo de racha: modo Descanso** | Al llegar al límite y con al menos dos equipos completos en fila, ganadores descansan una ronda. | Frustración de quienes esperan mucho para entrar. |
| **Justicia de fila** | La lógica de entrada/permanencia prioriza a quien jugó menos partidos. | Reclamos de baja participación durante la jornada. |
| **Marcador integrado (uso opcional)** | Se puede llevar marcador en la app, pero no es obligatorio. | Grupos casuales que no quieren registrar cada punto. |
| **Onboarding para grupos nuevos** | La configuración se destaca antes de iniciar el flujo de juego. | Errores de configuración al inicio de la sesión. |

## Privacidad y cuidado social

- Compartir ranking/estadísticas debe manejarse como comportamiento opt-in.
- El nivel de comodidad varía por grupo; evitar exposición pública de desempeño individual sin acuerdo explícito.
- La comunicación del producto debe presentar Elo como ayuda de equilibrio, no como juicio público.

## Restricciones actuales de alcance

- Aún no hay integración con marcador externo.
- El foco es la gestión de sesiones recreativas, no la operación completa de torneos oficiales.
- Las reglas son configurables por grupo, manteniendo el foco en justicia y fluidez.

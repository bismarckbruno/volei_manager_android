# Visión de Producto y Personas

## Posicionamiento del Producto

- **Nombre de la app:** Voleicito
- **Público primario:** Organizador del grupo de vóley amateur (el "Manager")
- **Propuesta de valor:** Automatizar la operación de partidos, reducir fricción social por "grupitos" y mantener el tiempo de cancha justo con reglas transparentes.

## Persona 1: El Manager (Persona Principal)

### Perfil
- Generalmente entre 20 y 35 años
- Organiza partidos sobre todo los sábados (a veces feriados)
- Coordina encuentros con alrededor de 20 jugadores
- Usa la app como herramienta operativa durante la jornada

### Motivaciones centrales
- Llegar con la logística ya resuelta
- Evitar discusiones por favoritismo y decisiones improvisadas
- Mantener al grupo comprometido porque el juego se percibe justo
- Reducir carga mental frente al control manual

### Dolores principales
- Formación de grupitos y sensación de desequilibrio
- Concentración de jugadores muy fuertes en un solo equipo
- Discusiones frecuentes sobre quién entra a la cancha
- Incomodidad cuando la exposición de desempeño se siente excesiva

### Cómo lo resuelve Voleicito
- **Balanceo automático** con Elo + distribución de `isPriority`
- **Reglas de racha** (Rebalanceo/Descanso) para evitar monopolio de cancha
- **Gestión justa de fila** priorizando a quien jugó menos
- **Peaje por llegada tardía (`dailyToll`)** para evitar percepción de "colarse"
- **Onboarding enfocado en configuración** antes de iniciar partidos

## Persona 2: Jugador Recurrente (Persona Secundaria)

### Perfil
- Rango común de 15 a 40 años, concentrado en 20-30
- Participa seguido en sesiones recreativas
- Puede no operar la app, pero está directamente impactado por sus reglas

### Motivaciones centrales
- Jugar una cantidad razonable de partidos en el día
- Sentir que los equipos están equilibrados
- Evitar tensión social y decisiones subjetivas

### Sensibilidades
- La justicia importa más que la formalidad competitiva estricta
- Compartir rankings en público puede generar incomodidad en parte del grupo

## Problema de mayor valor que se resuelve

La app resuelve principalmente la **percepción de injusticia en acceso a cancha y formación de equipos**, que primero es un problema social. Al convertir decisiones en reglas explícitas, configurables y repetibles, Voleicito reduce conflictos y aumenta la confianza en el flujo de juego.

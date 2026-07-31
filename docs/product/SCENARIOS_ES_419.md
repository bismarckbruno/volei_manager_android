# Escenarios Clave de Uso

Los escenarios son historias estructuradas de usuarios intentando lograr un objetivo con el producto.

## Escenario 1: "Necesitamos equipos justos ahora"

**Actor:** Manager  
**Contexto:** Inicio del sábado, alrededor de 20 jugadores presentes.  
**Objetivo:** Empezar rápido con equipos equilibrados y menos reclamos.  
**Flujo:**
1. El manager confirma configuración del grupo (tamaño de equipo, modo de racha, límite de victorias).
2. El manager agrega/revisa jugadores participantes.
3. La app genera equipos con Elo + distribución de prioridad.
4. El grupo inicia con menos intervención subjetiva.

**Señal de éxito:** Menos reclamos inmediatos por sorteo injusto.

## Escenario 2: "Un equipo fuerte monopoliza la cancha"

**Actor:** Manager + jugadores recurrentes  
**Contexto:** Un equipo gana varias veces seguidas.  
**Objetivo:** Mantener competitividad y motivar a quienes esperan.  
**Flujo (Rebalanceo):**
1. El equipo llega al límite de victorias configurado.
2. La app divide ganadores y recompone la siguiente ronda.

**Flujo (Descanso):**
1. El equipo llega al límite de victorias configurado.
2. Si hay al menos dos equipos completos en fila, los ganadores descansan una ronda.
3. Entran dos equipos de la fila.

**Señal de éxito:** Mejor percepción de rotación y menos frustración por repetición.

## Escenario 3: "Llegó un jugador tarde"

**Actor:** Manager  
**Contexto:** Ya se jugó el primer partido y entra un jugador nuevo.  
**Objetivo:** Mantener justicia de fila sin discusión manual.  
**Flujo:**
1. El manager agrega al jugador tardío.
2. La app aplica `dailyToll` según el promedio de partidos ya jugados ese día.
3. La prioridad de fila se mantiene justa para quienes llegaron antes.

**Señal de éxito:** Menos conflicto sobre quién debe entrar después.

## Escenario 4: "Evitar exponer jugadores en el chat del grupo"

**Actor:** Manager  
**Contexto:** Parte del grupo es sensible a la exposición pública de rankings.  
**Objetivo:** Mantener confianza sin perder datos útiles de operación.  
**Flujo:**
1. El manager usa ranking/historial para decisiones dentro de la sesión.
2. El manager evita compartir de forma masiva datos sensibles individuales.
3. El grupo sigue aportando feedback sin sentirse juzgado.

**Señal de éxito:** Adopción continua con menor incomodidad social.

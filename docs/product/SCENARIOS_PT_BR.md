# Cenários Principais de Uso

Cenários são histórias estruturadas de um usuário tentando alcançar um objetivo com o produto.

## Cenário 1: "Precisamos de times justos agora"

**Ator:** Manager  
**Contexto:** Início do sábado, cerca de 20 jogadores presentes.  
**Objetivo:** Começar rápido com times equilibrados e menos reclamações.  
**Fluxo:**
1. O manager confirma as configurações do grupo (tamanho do time, modo de sequência, limite de vitórias).
2. O manager adiciona/confere os jogadores participantes.
3. O app gera os times com Elo + distribuição de prioridade.
4. O grupo inicia a rodada com menos intervenção subjetiva.

**Sinal de sucesso:** Menos reclamações imediatas sobre sorteio injusto.

## Cenário 2: "Um time forte está monopolizando a quadra"

**Ator:** Manager + jogadores recorrentes  
**Contexto:** Um time vence muitas partidas seguidas.  
**Objetivo:** Manter competitividade e engajar quem está esperando.  
**Fluxo (Rebalanceamento):**
1. O time atinge o limite de vitórias configurado.
2. O app divide os vencedores e recompõe a próxima rodada.

**Fluxo (Descanso):**
1. O time atinge o limite de vitórias configurado.
2. Se houver ao menos dois times completos na fila, os vencedores descansam uma rodada.
3. Dois times da fila entram na quadra.

**Sinal de sucesso:** Melhor percepção de rodízio e menos frustração por repetição.

## Cenário 3: "Chegou um jogador atrasado"

**Ator:** Manager  
**Contexto:** O primeiro jogo já aconteceu; novo jogador entra na sessão.  
**Objetivo:** Preservar a justiça da fila sem discussão manual.  
**Fluxo:**
1. O manager adiciona o jogador atrasado.
2. O app aplica `dailyToll` com base na média de partidas já jogadas no dia.
3. A prioridade da fila permanece justa para quem chegou antes.

**Sinal de sucesso:** Menor conflito sobre quem deve entrar em seguida.

## Cenário 4: "Evitar exposição de jogadores no grupo"

**Ator:** Manager  
**Contexto:** Parte do grupo é sensível à exposição pública de ranking.  
**Objetivo:** Manter confiança do grupo sem abrir mão dos dados operacionais.  
**Fluxo:**
1. O manager usa ranking/histórico para decisões durante a sessão.
2. O manager evita compartilhamento amplo de dados sensíveis individuais.
3. O grupo continua contribuindo com feedback sem sensação de julgamento.

**Sinal de sucesso:** Adoção contínua com menor desconforto social.

# Fluxos principais do app — diagramas Mermaid

Esta pasta documenta, em fluxogramas [Mermaid](https://mermaid.js.org/), como o Voleizin
funciona por dentro: da criação do grupo até o fim de cada partida. A ideia é dar uma visão
rápida de "o que o app faz quando..." sem precisar ler o `VoleiViewModel.kt` inteiro.

## Como visualizar

Os blocos ` ```mermaid ` já são markdown padrão, mas a **renderização visual do diagrama
depende do visualizador ter suporte a Mermaid habilitado** — sem isso você só vê o texto do
código (o que é esperado nesse caso, não é um erro no arquivo):

- **GitHub**: renderiza nativamente, sem nenhuma configuração — basta abrir o arquivo no
  navegador.
- **Android Studio / IntelliJ**: o plugin "Markdown" sozinho **não** renderiza Mermaid — é
  preciso instalar o plugin JetBrains **"Mermaid"** à parte (*Settings → Plugins →
  Marketplace → buscar "Mermaid" → Install → reiniciar a IDE*). Depois disso, abra o `.md` e
  use o preview (ícone de split view/olho no canto superior direito do editor); os blocos
  mermaid passam a aparecer como diagrama.
- **VS Code**: instale a extensão *Markdown Preview Mermaid Support*.

## Convenções usadas nos diagramas

- `flowchart TD` (fluxograma de atividades, de cima para baixo) é a notação usada em todos os
  diagramas — é a que melhor representa decisões condicionais encadeadas (`if`/`else`), que é
  a maior parte da lógica do app. Não há diagramas de sequência (não há troca de mensagens
  entre atores) nem de classes aqui.
- `subgraph` agrupa cenários alternativos lado a lado (ex.: diferentes quantidades de
  jogadores presentes) dentro do mesmo diagrama, para comparação visual.
- Losangos (`{texto}`) = decisão. Retângulos (`[texto]`) = ação/estado. Bordas arredondadas
  (`(texto)`) = início/fim de fluxo.
- Cada diagrama tem um comentário `%% fonte: Arquivo.kt:funcao()` logo no topo, apontando para
  o método real no código — **ao alterar essa lógica, atualize o diagrama correspondente**.
- Os valores usados nos exemplos são os **padrões do app**: `teamSize = 6`,
  `victoryLimit = 3`, `priorityEnabled = true`, `guaranteeSetter = true`, salvo quando o
  próprio diagrama varia esse valor de propósito (ex.: os cenários de contagem de jogadores).

## Índice

| Arquivo | Cobre |
|---|---|
| [`01-ciclo-de-vida-do-grupo.md`](./01-ciclo-de-vida-do-grupo.md) | Criação de grupo (onboarding), conversão Recreativo ↔ Posições Fixas, mudança manual do nº de jogadores por time |
| [`02-checkin-e-fila-de-espera.md`](./02-checkin-e-fila-de-espera.md) | Check-in (presença), pedágio por atraso, "garantir próxima partida" |
| [`03-formacao-de-times.md`](./03-formacao-de-times.md) | Montagem automática do primeiro jogo, com cenários de 10 a 27 jogadores presentes |
| [`04-fim-de-partida-e-proxima-rodada.md`](./04-fim-de-partida-e-proxima-rodada.md) | Elo, sequência de vitórias (streak), e os modos **Rebalanceamento** vs **Descanso** |
| [`05-substituicoes-e-setup-manual.md`](./05-substituicoes-e-setup-manual.md) | Substituição manual de jogador e montagem manual de times |

## Fora de escopo (ainda não implementado no app)

Os tipos de grupo `TOURNAMENT_RECREATIONAL` e `TOURNAMENT_PRO` (campeonatos com múltiplos
times, chaveamento) já existem no modelo de dados (`GroupType`, `TournamentFormat`,
`TournamentTeam`, `TournamentMatch`) mas não têm UI/fluxo de jogo implementados — por isso
não aparecem como seleção em `GroupType.selectableTypes` e não têm diagrama próprio aqui.

# 04 — Fim de partida, próxima rodada e os dois modos de balanceamento

## 4.1 Encerrar uma partida (`finishGame`)

```mermaid
%% fonte: VoleiViewModel.kt:finishGame + EloCalculator.kt
flowchart TD
    A(["Organizador declara o vencedor (Time A ou B)"]) --> B["Registra MatchHistory<br/>(placar, times, data)"]
    B --> C{"Vencedor é o mesmo<br/>da rodada anterior?"}
    C -- sim --> D["currentStreak += 1"]
    C -- não --> E["streakOwner = vencedor<br/>currentStreak = 1"]
    D --> F["Calcula delta de Elo da partida<br/>(EloCalculator, pela média de Elo dos 2 times)"]
    E --> F
    F --> G["Distribui o delta por jogador,<br/>normalizado (zero-sum) pelo Elo individual<br/>de cada um vs a média do time adversário"]
    G --> H["Atualiza elo/matchesPlayed/victories<br/>de cada jogador + grava PlayerEloLog"]
    H --> I["Zera os times em quadra<br/>(ficam vazios até a próxima rodada)"]
    I --> J(["Aguarda organizador tocar<br/>em 'Próxima rodada' → startNextRound()"])
```

## 4.2 `startNextRound`: Rebalancear vs Descansar

A escolha de modo é feita uma vez, na config do grupo (`balancingMode`), e vale para todas as
rodadas dali em diante:

```mermaid
%% fonte: VoleiViewModel.kt:startNextRound
flowchart TD
    A(["Organizador toca em 'Próxima rodada'"]) --> B{"BalancingMode do grupo"}
    B -- REBALANCE --> C(["startNextRoundRebalance<br/>→ ver seção 4.3"])
    B -- REST --> D(["startNextRoundRest<br/>→ ver seção 4.4"])
    C --> E["Zera lista de 'garantir próxima partida'"]
    D --> E
```

## 4.3 Modo **Rebalanceamento** (`REBALANCE`)

Neste modo não existe "time dono da quadra": a cada rodada os times são remontados somando
quem ganhou + quem perdeu + fila, balanceando por Elo — **exceto** quando a sequência de
vitórias bate o limite (`victoryLimit`), que dispara uma quebra especial.

```mermaid
%% fonte: VoleiViewModel.kt:startNextRoundRebalance
flowchart TD
    A(["startNextRoundRebalance"]) --> B["Monta pool: vencedores ainda presentes<br/>+ perdedores ainda presentes<br/>+ fila + quem chegou agora"]
    B --> C{"currentStreak &ge; victoryLimit ?"}
    C -- não --> D["Fluxo normal:<br/>vencedores tentam permanecer como 'Time vencedor'"]
    D --> E{"Time vencedor<br/>ficou incompleto<br/>(alguém saiu/ficou ausente)?"}
    E -- sim --> F{"Tipo usa posições?"}
    F -- sim --> G["pickToCoverComposition completa<br/>as vagas em aberto pela fila<br/>→ reseta a sequência (mudou o time)"]
    F -- não --> H["Completa por prioridade e depois<br/>por ordem de fila → reseta a sequência"]
    E -- não --> I["Time vencedor segue intacto,<br/>sequência NÃO é resetada"]
    G --> J["Time desafiante é montado do zero<br/>com o resto do pool<br/>(garantidos → prioridade/posição → fila)"]
    H --> J
    I --> J
    J --> K(["Nova rodada pronta.<br/>Perdedores da rodada anterior<br/>voltam misturados na fila/no novo time"])

    C -- "sim: QUEBRA DE SEQUÊNCIA" --> L["Zera currentStreak/streakOwner"]
    L --> M["sortedWinners = vencedores ordenados<br/>por jogos efetivos hoje"]
    M --> N["Mantém só os primeiros teamSize*2<br/>('winnersToKeep'); excedente volta ao pool"]
    N --> O{"Tipo usa posições?"}
    O -- sim --> P["PositionAssigner.splitByElo divide os<br/>mantidos em 2 metades equilibradas por Elo"]
    P --> Q["pickToCoverComposition completa<br/>vagas de posição faltantes em cada metade<br/>usando fila + perdedores"]
    O -- não --> R["splitPlayersEvenlyForRebalance:<br/>divide os mantidos em 2 times por Elo<br/>(garante 1 prioridade por time, se aplicável)"]
    R --> S["Completa vagas restantes com o pool<br/>(perdedores + fila), balanceando por Elo"]
    Q --> T(["Dois times novos, nascidos do time<br/>que vinha ganhando — sequência zerada"])
    S --> T
```

## 4.4 Modo **Descanso** (`REST`) — "rei da quadra"

Aqui o time vencedor permanece em quadra partida após partida (só trocando o adversário pela
fila) até bater o limite de vitórias — só então ele vai descansar de verdade.

```mermaid
%% fonte: VoleiViewModel.kt:startNextRoundRest + tryScheduleReturningTeamMatchIfAny
flowchart TD
    A(["startNextRoundRest"]) --> B{"Há um time completo<br/>voltando do descanso<br/>nesta rodada?"}
    B -- sim --> C["Reencontra esse time contra<br/>o time que reinava quando eles saíram<br/>(_lastWinners daquela época)"]
    C --> D{"Algum dos dois<br/>ficou incompleto?"}
    D -- sim --> E["Completa com a fila;<br/>se ainda faltar, time volta a descansar<br/>por mais 1 rodada e segue fluxo normal"]
    D -- não --> F(["Partida 'revanche do retorno' agendada,<br/>pula o resto do fluxo"])
    E --> G
    B -- não --> G{"currentStreak &ge; victoryLimit ?"}
    G -- não --> H(["Fluxo igual ao Rebalanceamento normal<br/>(reusa startNextRoundRebalance)"])
    G -- "sim: time vencedor vai descansar" --> I["Zera currentStreak/streakOwner"]
    I --> J{"Fila tem &ge; 2 times<br/>completos esperando?"}
    J -- sim --> K["Os 2 primeiros times completos da fila<br/>entram direto em quadra um contra o outro"]
    K --> L["Time vencedor some da quadra:<br/>marcado para 'voltar' em roundCounter + 1<br/>(markPlayersResting)"]
    J -- não --> M["Time vencedor continua em quadra<br/>(não há gente suficiente pra substituí-lo)"]
    M --> N{"Fila tem &ge; 1 time<br/>completo?"}
    N -- sim --> O["Um time novo da fila entra<br/>como desafiante"]
    N -- não --> P["Desafiante é montado com o que sobrar<br/>da fila + perdedores da rodada anterior"]
    L --> Q(["Nova rodada pronta"])
    O --> Q
    P --> Q
    F --> Q
    H --> Q
```

### Diferença Recreativo vs Posições Fixas na quebra de sequência (ambos os modos)

```mermaid
%% fonte: VoleiViewModel.kt:startNextRoundRebalance (bloco "currentStreak >= victoryLimit")
flowchart TD
    A{"Tipo do grupo"} -- "Recreativo" --> B["splitPlayersEvenlyForRebalance:<br/>ordena por Elo, intercala em 2 times,<br/>garante 1 jogador 'isPriority' por time<br/>se a regra estiver ativa"]
    A -- "Posições Fixas" --> C["PositionAssigner.splitByElo:<br/>divide o time vencedor em 2 metades<br/>por Elo, sem olhar posição ainda"]
    C --> D["Cada metade chama pickToCoverComposition<br/>para fechar as vagas de posição que<br/>faltarem, puxando da fila/perdedores"]
    B --> E(["Resultado: 2 times novos,<br/>equilibrados por Elo"])
    D --> E
```

## 4.5 Nota: tipos de campeonato

`TOURNAMENT_RECREATIONAL` e `TOURNAMENT_PRO` não têm `balancingMode` (`supportsBalancingMode
= false`) — não existe "próxima rodada" automática como nos fluxos acima. Esses tipos ainda
não têm essa parte da engine implementada (ver nota no [`00-indice-dos-fluxogramas.md`](./00-indice-dos-fluxogramas.md)).

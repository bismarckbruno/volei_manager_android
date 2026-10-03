# 03 — Formação automática de times (primeiro jogo)

## 3.1 Fluxo de `startNewAutomaticGame`

Esse é o botão "Iniciar jogo" quando ainda não há partida em quadra. Ele só funciona se houver
jogadores presentes suficientes para fechar os dois times no `teamSize` configurado — **o app
nunca reduz o `teamSize` sozinho** (isso é sempre manual, ver
[`01-ciclo-de-vida-do-grupo.md`](./01-ciclo-de-vida-do-grupo.md#12-conversão-de-tipo-de-grupo-e-mudança-de-tamanho-de-time)).

```mermaid
%% fonte: VoleiViewModel.kt:startNewAutomaticGame
flowchart TD
    A(["Organizador toca em 'Iniciar jogo'"]) --> B{"presentes &ge; teamSize * 2 ?"}
    B -- não --> Z["Botão fica bloqueado /<br/>mostra quantos jogadores faltam"]
    B -- sim --> C["Aplica pedágio pendente<br/>a quem ainda não tem o de hoje"]
    C --> D["Agrupa e intercala o pool pelo<br/>nº de jogos efetivos hoje<br/>(quem jogou menos entra primeiro)"]
    D --> E{"Há jogadores com<br/>'garantir próxima partida'?"}
    E -- sim --> F["Eles entram primeiro<br/>(até o limite de vagas)"]
    E -- não --> G{"Tipo usa posições fixas?"}
    F --> G
    G -- "não (Recreativo)" --> H{"&ge; 2 jogadores com<br/>isPriority no pool?"}
    H -- sim --> I["Reserva até 2 vagas<br/>para prioridades"]
    H -- não --> J["Ignora regra de prioridade"]
    G -- "sim (Posições Fixas)" --> K["pickToCoverComposition escolhe quem completa<br/>as vagas restantes (preferida → secundária<br/>→ coringa → ataque → defesa, por Elo)"]
    I --> L["Preenche vagas restantes<br/>por ordem de fila (jogos efetivos)"]
    J --> L
    K --> M["buildBalancedTeams monta Time A/B<br/>por slot de posição + guaranteeSetter"]
    L --> N["balanceTeamsWithPriority monta Time A/B<br/>alternando por Elo (prioridades primeiro)"]
    M --> O(["Sobra do pool vira a fila de espera"])
    N --> O
    O --> P["Zera streak (currentStreak=0,<br/>streakOwner=null) e inicia cronômetro da partida"]
```

## 3.2 Cenários com o padrão de 6 jogadores por time (12 em quadra)

Com `teamSize = 6` (padrão), são necessários 12 jogadores para fechar os dois times. O que
sobra vai para a fila de espera. Os diagramas abaixo comparam o resultado conforme o total de
jogadores presentes no dia:

```mermaid
%% fonte: VoleiViewModel.kt:startNewAutomaticGame (math: times = teamSize*2, resto = fila)
flowchart TD
    subgraph S12["12 presentes"]
        A12["12 presentes"] --> B12["Time A: 6 · Time B: 6"] --> C12["Fila: 0"]
    end
    subgraph S15["15 presentes"]
        A15["15 presentes"] --> B15["Time A: 6 · Time B: 6"] --> C15["Fila: 3"]
    end
    subgraph S18["18 presentes"]
        A18["18 presentes"] --> B18["Time A: 6 · Time B: 6"] --> C18["Fila: 6<br/>(1 time completo esperando)"]
    end
    subgraph S21["21 presentes"]
        A21["21 presentes"] --> B21["Time A: 6 · Time B: 6"] --> C21["Fila: 9"]
    end
    subgraph S24["24 presentes"]
        A24["24 presentes"] --> B24["Time A: 6 · Time B: 6"] --> C24["Fila: 12<br/>(2 times completos esperando)"]
    end
    subgraph S27["27 presentes"]
        A27["27 presentes"] --> B27["Time A: 6 · Time B: 6"] --> C27["Fila: 15<br/>(2 times completos + 3 avulsos)"]
    end

    S12 ~~~ S15 ~~~ S18 ~~~ S21 ~~~ S24 ~~~ S27
```

> A quantidade de "times completos esperando" (fila dividida por `teamSize`) importa de verdade
> no **modo Descanso**: com 2+ times completos na fila, o time vencedor que bateu o limite de
> vitórias vai descansar e dois times novos entram direto da fila (ver
> [`04-fim-de-partida-e-proxima-rodada.md`](./04-fim-de-partida-e-proxima-rodada.md)). No modo
> Rebalanceamento essa contagem não muda o comportamento — a fila é sempre redistribuída.

Em **Posições Fixas**, a mesma matemática de "quantos sobram" vale, mas quem exatamente sobra
(e não só quantos) depende de quais vagas de composição (levantador, ponteiro, oposto, central,
líbero) ainda precisam ser cobertas — ver `TeamComposition.requiredSlots` para o time de 6.

## 3.3 Cenário com déficit de jogadores (menos de 12 presentes)

```mermaid
%% fonte: GameScreen.kt (minimumPlayersNeeded) — mesma lógica do doc 01, repetida aqui no contexto de formação de time
flowchart TD
    A["10 presentes, teamSize = 6<br/>(precisa de 12)"] --> B{"10 &ge; 12 ?"}
    B -- não --> C["Jogo não pode começar no 6x6.<br/>Opções do organizador:"]
    C --> D["1) Esperar mais gente chegar"]
    C --> E["2) Reduzir teamSize para 4<br/>(ver doc 01, seção 1.2)"]
    E --> F["Agora precisa de 8:<br/>10 &ge; 8 → libera iniciar 4x4<br/>(2 jogadores vão para a fila)"]
    F --> G["Mais tarde chegam 2+ pessoas<br/>(ex.: 12 presentes no total)"]
    G --> H["Organizador sobe teamSize<br/>de volta para 6 (ver doc 01)"]
    H --> I(["Próxima formação de time<br/>já usa 6x6 normalmente — doc 03.1"])
```

# 02 — Check-in e fila de espera

## 2.1 Marcar presença e calcular o pedágio por atraso

Quando o organizador marca um jogador como presente (`togglePlayerPresence`), o app decide se
ele entra "de graça" (primeiro do dia) ou paga pedágio (chegou depois que o dia já começou).

```mermaid
%% fonte: VoleiViewModel.kt (applyTollIfNecessary) + TollCalculator.kt
flowchart TD
    A(["Organizador marca jogador como presente"]) --> B{"Jogador já tem<br/>tollDate == hoje?"}
    B -- sim --> C["Pedágio do dia já calculado,<br/>mantém dailyToll existente"]
    B -- não --> D["TollCalculator.calculateToll:<br/>média dos 'jogos efetivos hoje'<br/>de todos os já presentes"]
    D --> E["dailyToll = média arredondada<br/>tollDate = hoje"]
    C --> F(["Jogador entra na fila de espera<br/>(ou é somado ao pool se já houver jogo rolando)"])
    E --> F
    F --> G["'jogos efetivos' deste jogador agora =<br/>jogos reais hoje + dailyToll<br/>(TollCalculator.getEffectiveGames)"]
    G --> H["Esse valor some automaticamente<br/>à meia-noite: tollDate deixa de bater com 'hoje'"]
```

> O pedágio existe para não punir quem chega cedo: um jogador atrasado recebe de presente a
> *média* de jogos já disputados pelo pessoal presente, em vez de entrar com 0 jogos (o que o
> colocaria sempre na frente da fila).

## 2.2 "Garantir próxima partida"

O organizador pode marcar um ou mais jogadores (presentes, mas de fora da quadra) para entrar
com prioridade total na montagem da próxima rodada — útil para quem está de saída e quer
garantir mais uma partida.

```mermaid
%% fonte: VoleiViewModel.kt (toggleGuaranteedNextMatchPlayer, _guaranteedNextMatchPlayerIds, trimGuaranteedNextMatchToCapacity)
flowchart TD
    A(["Organizador toca na estrela/ícone<br/>'garantir próxima partida' de um jogador"]) --> B["Jogador entra em<br/>_guaranteedNextMatchPlayerIds"]
    B --> C{"Quantos jogadores<br/>garantidos já marcados?"}
    C -- "além da capacidade da quadra" --> D["trimGuaranteedNextMatchToCapacity<br/>descarta o excesso mais antigo"]
    C -- "dentro da capacidade" --> E["Mantém a marcação"]
    D --> F(["Na próxima formação de time<br/>(1º jogo ou próxima rodada),<br/>garantidos entram antes de qualquer<br/>regra de prioridade/posição/fila"])
    E --> F
    F --> G["Lista de garantidos é zerada<br/>assim que a rodada é formada<br/>(efeito de 1 rodada só)"]
```

## 2.3 Quem tem prioridade na fila (contexto para a formação de times)

Antes de montar os times (ver [`03-formacao-de-times.md`](./03-formacao-de-times.md)), vale
entender que **a regra que decide quem entra primeiro muda conforme o tipo de grupo**:

```mermaid
%% fonte: GroupType.kt (supportsPriority) + Player.kt (isPriority, preferredPosition)
flowchart TD
    A{"Tipo do grupo"} -- Recreativo --> B["Usa isPriority:<br/>flag neutra 'distribuir por igual'<br/>(cada grupo decide o que significa:<br/>levantador, gênero, nível de jogo...)"]
    A -- "Posições Fixas" --> C["isPriority não tem efeito aqui.<br/>Em vez disso, cada jogador tem<br/>preferredPosition/secondaryPosition<br/>(ou nenhuma = 'coringa')"]
    B --> D["Regra de prioridade só é ativada se<br/>houver >= 2 jogadores com isPriority<br/>no pool disponível (shouldApplyPriorityRule)"]
    C --> E["PositionAssigner decide quem entra<br/>com base nas vagas de composição<br/>ainda não preenchidas (ver doc 03)"]
```

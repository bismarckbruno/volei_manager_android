# 01 — Ciclo de vida do grupo

## 1.1 Criação de um grupo (onboarding)

Ao criar um grupo (`CreateGroupDialog` → `onboardingStep`), o app pede as informações em
etapas sequenciais antes de liberar a tela principal do jogo.

```mermaid
%% fonte: GroupConfig.kt (ONBOARDING_STEP_*) + VoleiViewModel.kt (continueCurrentGroupOnboarding*)
flowchart TD
    A(["Usuário toca em 'Criar grupo'"]) --> B[ONBOARDING_STEP_GROUP_NAME<br/>define nome do grupo]
    B --> C[ONBOARDING_STEP_GROUP_TYPE<br/>escolhe tipo: Recreativo ou Posições Fixas]
    C --> D{"Tipo suporta modo de balanceamento?"}
    D -- "sim (sempre,<br/>exceto campeonatos)" --> E[ONBOARDING_STEP_BALANCING_MODE<br/>Rebalancear vs Descansar]
    E --> F[ONBOARDING_STEP_TEAM_SIZE<br/>jogadores por time, padrão 6]
    F --> G{"Tipo suporta prioridade?"}
    G -- "Recreativo: sim" --> H[ONBOARDING_STEP_MIN_PLAYERS<br/>'mín. 1 prioridade por time' on/off]
    G -- "Posições Fixas: não" --> I[mostra 'garantir levantador' em vez disso]
    H --> J[ONBOARDING_STEP_COMPLETE]
    I --> J
    J --> K(["Tela principal do grupo liberada"])
```

> Nos tipos de campeonato (fora de escopo/implementação, ver [índice](./00-indice-dos-fluxogramas.md)) este fluxo seria mais
> curto: sem etapa de modo de balanceamento nem de prioridade.

## 1.2 Conversão de tipo de grupo e mudança de tamanho de time

Depois de criado, o organizador pode reabrir `GroupConfigDialog` a qualquer momento (inclusive
com uma partida em andamento) para trocar o tipo do grupo e/ou o número de jogadores por time
usando um slider. **O app nunca faz essa troca sozinho** — é sempre uma ação manual aqui.

```mermaid
%% fonte: Dialogs.kt (GroupConfigDialog, changeIsLossy) + GroupType.kt (canConvertTo, coerceTeamSize)
flowchart TD
    A(["Organizador abre 'Configurações do grupo'"]) --> B[Ajusta slider de tamanho de time<br/>e/ou troca o tipo do grupo]
    B --> C{"Tipo é de campeonato (imutável)?"}
    C -- sim --> C1[Conversão bloqueada na UI]
    C -- não --> D{"Mudança é 'lossy'? partida em andamento OU<br/>tamanho antigo maior que o novo máximo do tipo"}
    D -- não --> E[Salva direto:<br/>GroupType.coerceTeamSize ajusta o slider<br/>ao intervalo do novo tipo]
    D -- sim --> F[Mostra diálogo de confirmação<br/>'Isso vai encerrar a partida atual']
    F -- cancela --> B
    F -- confirma --> G[Salva nova config<br/>+ cancela a partida em andamento, se houver]
    E --> H(["Próxima formação de time já usa<br/>o novo teamSize/tipo"])
    G --> H
```

### Caso de uso: "6x6 virou 4x4 porque só vieram 10 pessoas"

Isso é só a aplicação do fluxo acima com `teamSize: 6 → 4`:

```mermaid
%% fonte: GameScreen.kt (minimumPlayersNeeded = config.teamSize * 2) + Dialogs.kt (GroupConfigDialog)
flowchart TD
    A["10 jogadores presentes,<br/>teamSize configurado = 6<br/>(precisa de 12)"] --> B{"presentIds.size &ge; teamSize * 2 ?"}
    B -- "não (10 < 12)" --> C["Tela mostra estado vazio:<br/>'faltam jogadores para começar'<br/>— botão de iniciar fica bloqueado"]
    C --> D["Organizador abre config do grupo<br/>e desce o slider de 6 para 4"]
    D --> E["changeIsLossy? só se já houver<br/>partida em andamento — aqui não há, salva direto"]
    E --> F["teamSize agora = 4 (precisa de 8)"]
    F --> G{"presentIds.size &ge; 4 * 2 ?"}
    G -- "sim (10 &ge; 8)" --> H(["Botão de iniciar liberado:<br/>monta 4x4, 2 jogadores vão para a fila"])
```

### Caminho de volta: chegou mais gente, quer voltar a 6x6

```mermaid
%% fonte: Dialogs.kt (GroupConfigDialog, changeIsLossy)
flowchart TD
    A["Grupo rodando em 4x4,<br/>chegam mais jogadores (ex.: 12+ presentes)"] --> B["Organizador reabre config do grupo<br/>e sobe o slider de 4 para 6"]
    B --> C{"Há partida em andamento?"}
    C -- "sim" --> D["changeIsLossy = true<br/>pede confirmação: 'isso encerra a partida atual'"]
    D -- confirma --> E["Partida atual é cancelada,<br/>teamSize vira 6"]
    C -- "não (estava entre rodadas)" --> E
    E --> F(["Próxima vez que o organizador iniciar o jogo,<br/>startNewAutomaticGame já monta 6x6<br/>(ver 03-formacao-de-times.md)"])
```

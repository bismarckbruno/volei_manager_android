# 05 — Substituição manual e montagem manual de times

## 5.1 Substituir um jogador (`substitutePlayer`)

Durante uma partida em andamento, o organizador pode trocar qualquer jogador da quadra por
outro (do banco/fila ou do time adversário) a qualquer momento.

```mermaid
%% fonte: VoleiViewModel.kt:substitutePlayer
flowchart TD
    A(["Organizador escolhe 'jogador que sai'<br/>e 'jogador que entra'"]) --> B["Aplica pedágio ao jogador<br/>que entra, se ainda não tiver o de hoje"]
    B --> C{"De onde vem<br/>o jogador que entra?"}
    C -- "Fila de espera" --> D["Jogador que sai vai para a fila<br/>no lugar de quem entrou"]
    C -- "Time adversário" --> E["Os dois trocam de time<br/>(saiu daqui, foi pro outro)"]
    C -- "Mesmo time<br/>(troca de posição em quadra)" --> F["Troca as posições/slots atribuídos<br/>entre os dois, mantendo ambos no time"]
    D --> G{"O time afetado era o<br/>'dono da sequência' (streakOwner)?"}
    E --> G
    F --> H(["Registra log de substituição manual<br/>(para o painel de atividade)"])
    G -- sim --> I["Zera currentStreak/streakOwner<br/>(a sequência não sobrevive à troca de elenco)"]
    G -- não --> H
    I --> H
    H --> J{"Tipo usa posições e a troca<br/>não foi só dentro do mesmo time?"}
    J -- sim --> K["refreshPositionAssignments recalcula<br/>os slots/posições dos dois times"]
    J -- não --> L(["Fim"])
    K --> L
```

## 5.2 Montagem manual de times (`ManualSetupScreen` / `startManualGame`)

Em vez de deixar o app montar os times automaticamente, o organizador pode arrastar/selecionar
jogadores manualmente para o Time A, Time B e a fila — por exemplo para respeitar combinações
específicas que o algoritmo automático não captura.

```mermaid
%% fonte: GameScreen.kt (ManualSetupScreen onConfirm) + VoleiViewModel.kt:startManualGame
flowchart TD
    A(["Organizador entra no modo<br/>'Montagem manual'"]) --> B["Lista todos os jogadores presentes<br/>(com Elo/posição visíveis, se habilitado)"]
    B --> C["Organizador distribui cada jogador<br/>manualmente: Time A / Time B / Fila"]
    C --> D["Organizador pode ajustar o teamSize<br/>nesta mesma tela (slider embutido)"]
    D --> E["onConfirm(timeA, timeB, fila, teamSize)"]
    E --> F["updateConfig grava o teamSize escolhido<br/>na configuração do grupo"]
    F --> G["startManualGame aplica pedágio<br/>pendente a todos os 3 grupos"]
    G --> H["Zera streak (currentStreak=0,<br/>streakOwner=null), limpa logs de atividade"]
    H --> I["Define Time A / Time B / Fila<br/>exatamente como escolhido"]
    I --> J{"Tipo usa posições fixas?"}
    J -- sim --> K["refreshPositionAssignments calcula<br/>os slots de cada jogador nos times<br/>(pode ficar 'composição incompleta' se faltar vaga)"]
    J -- não --> L(["Partida inicia normalmente"])
    K --> L
```

> Diferente da formação automática (doc `03`), aqui **não há bloqueio por quantidade mínima de
> jogadores** nem regra automática de prioridade/posição — a responsabilidade de montar times
> válidos é inteiramente do organizador.

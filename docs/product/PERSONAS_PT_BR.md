# Visão de Produto e Personas

## Posicionamento do Produto

- **Nome do App:** Vôlei Manager
- **Público primário:** Organizador do grupo de vôlei amador (o "Manager")
- **Proposta de valor:** Automatizar a operação das partidas, reduzir o atrito social causado por "panelinhas" e manter o tempo de quadra justo por regras transparentes.

## Persona 1: O Manager (Persona Principal)

### Perfil
- Geralmente entre 20 e 35 anos
- Organiza jogos principalmente aos sábados (às vezes em feriados)
- Conduz encontros com cerca de 20 jogadores
- Usa o app como ferramenta de gestão durante o dia do jogo

### Motivações centrais
- Começar o encontro com a logística resolvida
- Evitar discussões por favoritismo e decisões no improviso
- Manter o grupo engajado porque o jogo parece justo e dinâmico
- Reduzir carga mental em comparação com controle manual

### Principais dores
- Formação de panelinhas e sensação de desequilíbrio
- Concentração de jogadores muito fortes no mesmo time
- Discussões recorrentes sobre quem deve entrar na quadra
- Incômodo quando a exposição de desempenho parece excessiva

### Como o Vôlei Manager resolve
- **Balanceamento automático** com Elo + distribuição de `isPriority`
- **Regras de sequência** (Rebalanceamento/Descanso) para evitar monopólio da quadra
- **Gestão justa da fila** com prioridade para quem jogou menos
- **Pedágio de atraso (`dailyToll`)** para evitar percepção de "furar fila"
- **Onboarding orientado à configuração** antes de iniciar o jogo

## Persona 2: Jogador Recorrente (Persona Secundária)

### Perfil
- Faixa comum entre 15 e 40 anos, com concentração em 20-30
- Participa com frequência de sessões recreativas
- Nem sempre opera o app, mas é diretamente impactado pelas decisões

### Motivações centrais
- Jogar uma quantidade razoável de partidas no dia
- Perceber que os times estão equilibrados
- Evitar tensão social e decisões subjetivas

### Sensibilidades
- Justiça importa mais do que formalidade competitiva rígida
- Compartilhamento público de ranking pode gerar desconforto em parte do grupo

## Maior problema resolvido

O app resolve principalmente a **sensação de injustiça no acesso à quadra e na formação dos times**, que é antes de tudo um problema social. Ao transformar decisões em regras explícitas, configuráveis e repetíveis, o Vôlei Manager reduz conflitos e aumenta a confiança no fluxo das partidas.

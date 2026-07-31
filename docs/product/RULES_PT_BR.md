# Regras de Negócio e Algoritmos

Este documento relaciona regras do produto, implementação atual e dores do usuário.

## Matriz de Regras

| Funcionalidade / Regra | Comportamento Atual | Dor do Usuário Endereçada |
| --- | --- | --- |
| **Balanceamento automático de times** | Monta os times buscando paridade competitiva com média Elo e distribuição de prioridade. | Reclamações sobre panelinhas e jogos desequilibrados. |
| **Flag de prioridade (`isPriority`)** | Garante pelo menos um jogador prioritário por time (quando disponível). | Desequilíbrio de função/atributo (levantador, equilíbrio de gênero etc.). |
| **Pedágio de atraso (`dailyToll`)** | Se o jogador chega após o 1º jogo, recebe pedágio diário igual à média de partidas já jogadas no dia. | Percepção de injustiça com atrasados "furando fila". |
| **Limite de vitórias seguidas** | Valor inteiro configurável de 1 a 6 (padrão frequente 3; 2 e 3 são comuns). | Um único time dominando a quadra por muito tempo. |
| **Tratamento de sequência: modo Rebalanceamento** | Ao bater o limite, o time vencedor é dividido na rodada seguinte. | Longas sequências com pouca renovação de times. |
| **Tratamento de sequência: modo Descanso** | Ao bater o limite e havendo ao menos dois times completos na fila, vencedores descansam uma rodada. | Frustração de quem espera muito tempo para entrar. |
| **Justiça na fila** | A lógica de entrada/permanência prioriza quem jogou menos partidas. | Reclamações de baixa participação ao longo do dia. |
| **Placar integrado (uso opcional)** | O placar pode ser registrado no app, mas não é obrigatório. | Grupos casuais que não querem formalizar cada ponto. |
| **Onboarding para novos grupos** | Configurações ganham destaque antes do fluxo de início de jogo. | Erros de configuração na abertura da sessão. |

## Privacidade e segurança social

- Compartilhamento de ranking/estatísticas deve ser tratado como comportamento opt-in.
- O nível de conforto varia por grupo; evitar exposição pública de desempenho individual sem alinhamento explícito.
- A comunicação do produto deve apresentar o Elo como ferramenta de equilíbrio, não julgamento público.

## Restrições atuais de escopo

- Ainda não há integração com placar externo.
- O foco é gestão de sessões recreativas, não operação de torneio oficial completo.
- As regras são configuráveis por grupo, mantendo foco em justiça e fluidez.

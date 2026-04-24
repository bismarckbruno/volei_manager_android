# Vôlei Manager 🏐

**Vôlei Manager** é um aplicativo Android desenvolvido com **Jetpack Compose** e **Room Database** para gerenciar partidas de vôlei recreativo, automatizar o sorteio de times, acompanhar o desempenho dos jogadores (Ranking Elo) e garantir uma rotação justa de participantes.

## ✨ Funcionalidades

### 🎮 Gerenciamento de Partidas
- **Sorteio Automático Inteligente**: O app seleciona as pessoas e equilibra os grupos da forma mais justa possível, misturando participantes com diferentes níveis de habilidade (utilizando o sistema de pontuação Elo) e distribuindo uniformemente os jogadores prioritários.
- **Rotação Justa**: Sistema de fila de prioridade inteligente para garantir que todos aproveitem o jogo.
  - **Prioridade por Partidas**: Na hora de decidir quem entra na quadra ou quem fica entre os perdedores para jogar mais, **o app dá prioridade para quem jogou menos vezes**. Isso equilibra a frequência de jogo para que ninguém fique parado!
  - **Divisão de Vencedores**: Times que vencem muitas seguidas são divididos para evitar domínio e garantir rotatividade ("Rei da Quadra").
- **Montagem Manual**: Tela dedicada para selecionar ou ajustar manualmente a composição dos times.
- **Placar em Tempo Real**: Contagem de pontos de cada time durante a partida.

### 📊 Ranking e Estatísticas
- **Sistema Elo**: Pontuação dinâmica calculada após cada partida com base na força do oponente (K=32, Elo inicial 1200).
- **Histórico de Partidas**: Registro completo de todos os jogos, com times, placar final, variação de Elo e médias de Elo por time.
- **Compartilhamento**: Exporte o histórico de partidas como imagem para compartilhar nas redes sociais.

### 👥 Gerenciamento de Jogadores e Grupos
- **Múltiplos Grupos**: Crie e gerencie grupos independentes (ex: "Vôlei de Terça", "Vôlei de Praia"), cada um com seus próprios jogadores, histórico e configurações.
- **Perfil de Jogador**: Nome, Elo e marcação de prioridade.
- **Jogador Prioritário (`isPriority`)**: Flag genérica para distribuição equilibrada — pode representar levantadores, garantia de equilíbrio de gênero ou qualquer critério definido pelo grupo.
- **Pedágio (Chegada Tardia)**: Jogadores que chegam atrasados recebem jogos extras de "pedágio" para compensar o atraso dos demais, calculado com base na média de partidas já jogadas no dia.
- **Backup e Restauração**: Exportação e importação de dados completos (JSON) ou tabelas específicas (CSV) para salvar ou transferir o progresso.

### 🎨 Personalização
- **Tema**: Claro, Escuro ou automático (Sistema).
- **Cores dos Times**: Esquemas de cores alternativos para os times (funcionalidade de apoiador - ainda em planejamento).
- **Exibição opcional**: Ative ou desative a exibição do Elo e do Pedágio na interface.

## 🛠 Tecnologias Utilizadas
- **Linguagem**: Kotlin
- **Interface (UI)**: Jetpack Compose (Material Design 3)
- **Arquitetura**: MVVM — toda a lógica de negócio está em `VoleiViewModel`; DI manual via `ViewModelFactory`, sem Hilt/Dagger
- **Navegação**: Enum-based customizado (`Screen.GAME`, `HISTORY`, `FAQ`, `ABOUT`) com `AnimatedContent`
- **Banco de Dados Local**: Room (SQLite) — versão 10, com migrações incrementais
- **Assincronismo**: Coroutines & Flow (`viewModelScope`, `Dispatchers.IO`)
- **Serialização JSON**: Gson 2.10.1 (backup completo)
- **Processamento de Anotações**: KSP (Kotlin Symbol Processing)

## 🚀 Como Rodar o Projeto
1. Clone o repositório:
   ```bash
   git clone https://github.com/bismarckbruno/volei_manager_android.git
   ```
2. Abra o projeto no **Android Studio**.
3. Sincronize o Gradle e execute o app em um Emulador ou Dispositivo Físico (Android 7.0+ / API 24+).

## ⚙️ Regras Configuráveis por Grupo
- **Tamanho do Time**: De 2 a 6 jogadores por lado.
- **Limite de Vitórias**: Máximo de vitórias consecutivas antes do time vencedor ser dividido.
- **Ativar Prioridade**: Garante ao menos um jogador prioritário por time no sorteio automático (se houver disponibilidade).

## 🤝 Contribuição e Feedback
Contribuições são bem-vindas! Sinta-se à vontade para enviar um *pull request*.

Encontrou um problema ou tem uma ideia? Abra uma [Issue aqui](https://github.com/bismarckbruno/volei_manager_android/issues/new/choose).

## 📄 Licença
Este projeto é open-source e está disponível sob a [Licença MIT](LICENSE).

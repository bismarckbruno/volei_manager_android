# Vôlei Manager 🏐

**Vôlei Manager** é um aplicativo Android desenvolvido com **Jetpack Compose** e **Room Database** para gerenciar partidas de vôlei recreativo, automatizar o sorteio de times, acompanhar o desempenho dos jogadores (Ranking Elo) e garantir uma rotação justa de participantes.

## ✨ Funcionalidades

### 🎮 Gerenciamento de Partidas
- **Sorteio Automático**: Equilibra os times com base na pontuação Elo e na distribuição de jogadores prioritários.
- **Montagem Manual**: Tela dedicada para selecionar manualmente a composição dos times.
- **Placar em Tempo Real**: Contagem de pontos de cada time durante a partida.
- **Rotação Justa**: Sistema de fila de prioridade para garantir que todos joguem.
  - **Lógica da Fila (FIFO)**: Prioridade por ordem de chegada para evitar que jogadores fiquem muito tempo esperando.
  - **Divisão de Vencedores**: Times que vencem muitas seguidas são divididos para evitar domínio e garantir rotatividade ("Rei da Quadra").

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
- **Cores dos Times**: Esquemas de cores alternativos para os times (funcionalidade de apoiador).
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

## 🤝 Contribuição
Contribuições são bem-vindas! Sinta-se à vontade para abrir uma *issue* ou enviar um *pull request*.

## 📄 Licença
Este projeto é open-source e está disponível sob a [Licença MIT](LICENSE).

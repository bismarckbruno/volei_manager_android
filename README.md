# Vôlei Manager 🏐

**Vôlei Manager** é um aplicativo Android desenvolvido com **Jetpack Compose** e **Room Database** para gerenciar partidas de vôlei recreativo, automatizar o sorteio de times, acompanhar o desempenho dos jogadores (Ranking Elo) e garantir uma rotação justa de participantes.

## ✨ Funcionalidades

### 🎮 Gerenciamento de Partidas
- **Sorteio Automático**: Algoritmos que equilibram os times com base na pontuação Elo e paridade de gênero (opcional).
- **Rotação Justa**: Sistema de fila de prioridade para garantir que todos joguem.
  - **Lógica da Fila (FIFO)**: Prioridade por ordem de chegada para evitar que jogadores fiquem muito tempo esperando.
  - **Divisão de Vencedores**: Times que vencem muitas seguidas são divididos para evitar domínio e garantir rotatividade.
- **Montagem Manual**: Opção para selecionar manualmente os times.
- **Placar e Sequência**: Contagem de vitórias consecutivas ("Rei da Quadra").

### 📊 Ranking e Estatísticas
- **Sistema Elo**: Pontuação dinâmica calculada após cada partida com base na força do oponente.
- **Tela de Ranking**: Classificação com medalhas (🥇 🥈 🥉) e patentes (de Iniciante a Lenda).
- **Histórico de Partidas**: Registro detalhado de todos os jogos, com variação de Elo por partida.
- **Gráficos**: Evolução visual do Elo dos jogadores ao longo do tempo, com filtro por período.

### 👥 Gerenciamento de Jogadores e Grupos
- **Múltiplos Grupos**: Crie e gerencie grupos diferentes (ex: "Vôlei de Terça", "Vôlei de Praia").
- **Perfil de Jogador**: Nome, Elo, partidas jogadas e gênero.
- **Backup e Restauração**: Exportação e importação de dados completos (JSON) ou tabelas específicas (CSV) para compartilhar ou salvar o progresso.

## 🛠 Tecnologias Utilizadas
- **Linguagem**: Kotlin
- **Interface (UI)**: Jetpack Compose (Material Design 3)
- **Arquitetura**: MVVM (Model-View-ViewModel)
- **Banco de Dados Local**: Room (SQLite)
- **Assincronismo**: Coroutines & Flow
- **Injeção de Dependência**: ViewModelFactory (DI Manual)

## 🚀 Como Rodar o Projeto
1. Clone o repositório:
   ```bash
   git clone https://github.com/SEU_USUARIO/VoleiManager.git
   ```
2. Abra o projeto no **Android Studio**.
3. Sincronize o Gradle e execute o app em um Emulador ou Dispositivo Físico (Recomendado Android 8.0+).

## ⚙️ Regras Configuráveis
Você pode personalizar as regras para cada grupo:
- **Tamanho do Time**: De 2 a 6 jogadores por lado.
- **Limite de Vitórias**: Máximo de vitórias consecutivas antes do time vencedor ser dividido.
- **Prioridade de Gênero**: Garantir pelo menos uma mulher por time (se houver disponibilidade).

## 🤝 Contribuição
Contribuições são bem-vindas! Sinta-se à vontade para abrir uma *issue* ou enviar um *pull request*.

## 📄 Licença
Este projeto é open-source e está disponível sob a [Licença MIT](LICENSE).

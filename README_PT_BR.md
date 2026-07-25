# Vôlei Manager 🏐

**Vôlei Manager** é um aplicativo Android desenvolvido com **Jetpack Compose** e **Room Database** para gerenciar partidas de vôlei recreativo, automatizar o sorteio de times, acompanhar o desempenho dos jogadores (Ranking Elo) e garantir uma rotação justa de participantes.

## ✨ Funcionalidades

### 🎮 Gerenciamento de Partidas
- **Sorteio Automático Inteligente**: O app seleciona as pessoas e equilibra os grupos da forma mais justa possível, misturando participantes com diferentes níveis de habilidade (utilizando o sistema de pontuação Elo) e distribuindo uniformemente os jogadores prioritários.
- **Rotação Justa**: Sistema de fila de prioridade inteligente para garantir que todos aproveitem o jogo.
  - **Prioridade por Partidas**: Na hora de decidir quem entra na quadra ou quem fica entre os perdedores para jogar mais, **o app dá prioridade para quem jogou menos vezes**.
  - **Tratamento de Sequência por Modo**: Ao atingir o limite de vitórias, o app aplica o modo escolhido: no **Rebalanceamento**, divide os vencedores; no **Descanso**, pode tirar os vencedores da quadra para rodar a fila ("Rei da Quadra").
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
- **Exibição opcional**: Ative ou desative a exibição do Elo e do Pedágio na interface.

## 🛠 Tecnologias Utilizadas
- **Linguagem**: Kotlin
- **Interface (UI)**: Jetpack Compose (Material Design 3)
- **Arquitetura**: MVVM — toda a lógica de negócio está em `VoleiViewModel`; DI manual via `ViewModelFactory`, sem Hilt/Dagger
- **Navegação**: Enum-based customizado (`Screen.GAME`, `HISTORY`, `FAQ`, `ABOUT`) com `AnimatedContent`
- **Banco de Dados Local**: Room (SQLite) com migrações incrementais
- **Assincronismo**: Coroutines & Flow (`viewModelScope`, `Dispatchers.IO`)
- **Serialização JSON**: Gson 2.10.1 (backup completo)
- **Processamento de Anotações**: KSP (Kotlin Symbol Processing)

## 🚀 Como Rodar o Projeto
1. Clone o repositório:
   ```bash
   git clone https://github.com/bismarckbruno/volei_manager_android.git
   ```
2. Abra o projeto no **Android Studio**.
3. Sincronize o Gradle e execute o app em um emulador ou dispositivo físico (Android 7.0+ / API 24+).
4. Níveis de SDK atuais para publicação na Play Store: **compileSdk 36** e **targetSdk 36** (Android 16).

## ⚙️ Regras Configuráveis por Grupo
- **Tamanho do Time**: De 2 a 6 jogadores por lado.
- **Limite de Vitórias**: Máximo de vitórias consecutivas antes de aplicar a regra do modo ativo (dividir no Rebalanceamento ou rodar descanso no modo Descanso).
- **Ativar Prioridade**: Garante ao menos um jogador prioritário por time no sorteio automático (se houver disponibilidade).

## 🤝 Contribuição e Feedback
Contribuições são bem-vindas! Sinta-se à vontade para enviar um *pull request*.

Encontrou um problema ou tem uma ideia? Abra uma [Issue aqui](https://github.com/bismarckbruno/volei_manager_android/issues/new/choose).

## ⚖️ Documentação Legal
- [Política de Privacidade (PT-BR)](https://bismarckbruno.github.io/volei_manager_android/PRIVACY_POLICY_PT_BR)
- [Termos de Uso (PT-BR)](https://bismarckbruno.github.io/volei_manager_android/TERMS_OF_USE_PT_BR)
- [Privacy Policy (EN-US)](https://bismarckbruno.github.io/volei_manager_android/PRIVACY_POLICY)
- [Terms of Use (EN-US)](https://bismarckbruno.github.io/volei_manager_android/TERMS_OF_USE)
- [Política de Privacidad (ES-419)](https://bismarckbruno.github.io/volei_manager_android/PRIVACY_POLICY_ES_419)
- [Términos de Uso (ES-419)](https://bismarckbruno.github.io/volei_manager_android/TERMS_OF_USE_ES_419)
- [Licença MIT](LICENSE)

## ☕ Apoie o Projeto

O **Vôlei Manager** é um projeto independente e gratuito. Se o app te ajudou a organizar melhor suas partidas e você quiser incentivar o desenvolvimento de novas funcionalidades, considere me pagar um café.

### Formas de apoiar:

* **GitHub Sponsors:** [Clique aqui para apoiar via GitHub](https://github.com/sponsors/bismarckbruno)
* **PIX:** Veja as opções abaixo:

<details>
  <summary><b>Clique para exibir o QR Code e Chave PIX</b></summary>
  <br>
  <div align="center">
    <img src="apoio/qr_code_pix.png" width="200" alt="QR Code PIX"><br>
    <sub>Escaneie o QR Code acima ou use o código Copia e Cola abaixo:</sub>
    <br><br>
    <p><code>00020126650014br.gov.bcb.pix0136d143999e-2f7a-4ce4-84c3-b3b03b41536e0203Pix5204000053039865802BR5925BRUNO_BISMARCK_DA_SILVA_M6006CAXIAS62210517ApoioVoleiManager63044F13</code></p>
  </div>
</details>

---
*Qualquer valor é bem-vindo e ajuda a manter o café (e o código) fluindo!* 🏐

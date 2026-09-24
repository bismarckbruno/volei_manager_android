# Voleizin 🏐

**Voleizin** é um aplicativo Android desenvolvido com **Jetpack Compose** e **Room Database** para gerenciar partidas de vôlei recreativo, automatizar o sorteio de times, acompanhar o desempenho dos jogadores (Ranking Elo) e garantir uma rotação justa de participantes.

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
- **Tipos de Grupo**: **Recreativo** (balanceamento genérico por prioridade) ou **Posições Fixas** (defina a posição preferida/secundária de cada jogador — Levantador, Ponteiro, Central, Oposto, Líbero — com um motor automático de composição que preenche as posições necessárias e avisa quando a composição do time fica incompleta).
- **Perfil de Jogador**: Nome, Elo e marcação de prioridade (ou posição, em grupos de Posições Fixas).
- **Jogador Prioritário (`isPriority`)**: Flag genérica para distribuição equilibrada — pode representar levantadores, garantia de equilíbrio de gênero ou qualquer critério definido pelo grupo.
- **Pedágio (Chegada Tardia)**: Jogadores que chegam atrasados recebem jogos extras de "pedágio" para compensar o atraso dos demais, calculado com base na média de partidas já jogadas no dia.
- **Backup e Restauração**: Exportação e importação de dados completos (JSON) ou tabelas específicas (CSV).

### 🎨 Personalização
- **Tema**: Claro, Escuro ou automático (Sistema).
- **Exibição opcional**: Ative ou desative a exibição do Elo e do Pedágio na interface.

### ☁️ Login, Sincronização Premium em Nuvem e Transmissão ao Vivo para Espectadores
- **Login Opcional**: E-mail/senha ou login com o Google (via **Firebase Authentication**); espectadores também podem entrar em um grupo de forma anônima com um código de convite, sem precisar de conta.
- **Sincronização Premium em Nuvem**: Assinatura paga (mensal/anual, via **Google Play Billing**) permite que o organizador sincronize jogadores, placar ao vivo, histórico de partidas e registros de Elo do grupo entre dispositivos, usando **Firebase/Firestore**.
- **Transmissão ao Vivo para Espectadores**: Organizadores podem gerar um código de convite/entrada permanente para que espectadores acompanhem o placar em tempo real e, opcionalmente, o histórico de partidas e o Elo dos jogadores, sem precisar de conta completa.
- **Personalização de Cores dos Times**: Assinantes Premium podem personalizar as cores de cada time, só para si ou (como organizador do grupo) para todos que visualizam um grupo sincronizado.
- **Exclusão Alinhada à LGPD/GDPR**: Excluir um grupo sincronizado ou sua conta também remove os dados correspondentes na nuvem (jogadores, placar ao vivo, histórico, registros de Elo, códigos de convite) dos nossos servidores.

## 🛠 Tecnologias Utilizadas
- **Linguagem**: Kotlin
- **Interface (UI)**: Jetpack Compose (Material Design 3)
- **Arquitetura**: MVVM — toda a lógica de negócio está em `VoleiViewModel`; DI manual via `ViewModelFactory`, sem Hilt/Dagger
- **Navegação**: Enum-based customizado (`Screen.GAME`, `HISTORY`, `FAQ`, `ABOUT`) com `AnimatedContent`
- **Banco de Dados Local**: Room (SQLite) com migrações incrementais
- **Nuvem/Backend**: Firebase Authentication (login), Firestore (sincronização em nuvem/dados de transmissão ao vivo), Cloud Functions, Google Play Billing (assinaturas)
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

## 🔒 Privacidade, Telemetria e Sincronização Premium
Este projeto é distribuído sob a **GNU General Public License v3.0 (GPL-3.0)**. O app principal mantém os dados localmente no dispositivo, e, por padrão, não há coleta de telemetria anônima ativa.

Há uma funcionalidade opcional de telemetria anônima disponível, mas somente com consentimento explícito do usuário e um fluxo de opt-in claro. Quando ativada, a coleta é limitada a diagnósticos agregados de uso e saúde do app, sem incluir nomes, grupos, histórico de partidas, identificadores pessoais ou dados sensíveis. O usuário pode revogar o consentimento a qualquer momento na área de configurações do app, e a coleta é interrompida assim que o consentimento é retirado.

O app também oferece uma camada premium opcional com sincronização em nuvem de dados do grupo (jogadores, placar ao vivo, histórico de partidas, registros de Elo), transmissão ao vivo para espectadores por código de convite e personalização de cores dos times, usando **Firebase/Firestore**, **Firebase Authentication** e **Google Play Billing** para as compras de assinatura. O acesso a esses recursos exige login (ou, para espectadores, entrada anônima com código) e, para os recursos premium, uma assinatura ativa. Excluir um grupo sincronizado ou a conta também apaga os dados correspondentes na nuvem, em linha com as expectativas de exclusão de dados da LGPD/GDPR.

## 🎯 Sobre o Projeto

O **Voleizin** é um aplicativo Android desenvolvido para organizadores de vôlei amador. Ele automatiza o balanceamento de times (via Elo e prioridade), gerencia a fila de forma justa e reduz os conflitos de quadra.

📖 **Documentação de Produto:**
- [Visão de Produto e Personas](docs/product/PERSONAS_PT_BR.md)
- [Regras de Negócio e Algoritmos](docs/product/RULES_PT_BR.md)
- [Cenários Principais de Uso](docs/product/SCENARIOS_PT_BR.md)

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
- [Licença GPL v3.0](LICENSE)

## ☕ Apoie o Projeto

O **Voleizin** é um projeto independente e gratuito. Se o app te ajudou a organizar melhor suas partidas e você quiser incentivar o desenvolvimento de novas funcionalidades, considere me pagar um café.

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

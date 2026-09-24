# Política de Privacidade

**Última atualização:** Setembro de 2026

O **Voleizin** é um aplicativo Android gratuito e de código aberto para organizar partidas recreativas de vôlei. Esta política descreve como os dados são tratados no aplicativo, incluindo os recursos opcionais de login, sincronização premium em nuvem, transmissão ao vivo para espectadores e telemetria.

O projeto é distribuído sob a **GNU General Public License v3.0 (GPL-3.0)**.

---

### 1. Dados Coletados
O aplicativo armazena somente os dados que você fornece manualmente no banco de dados local, incluindo:
* Nomes dos jogadores;
* Grupos e configurações do grupo;
* Histórico de partidas;
* Pontuação Elo e metadados relacionados ao jogo.

Por padrão, o app não coleta identificadores pessoais, identificadores de publicidade, localização, contatos ou comportamento de navegação. Nenhum dado pessoal sensível é coletado automaticamente.

### 2. Telemetria Anônima (Análise de Uso e Relatórios de Falhas)
O app inclui uma funcionalidade opcional e anônima de telemetria, com **Firebase Analytics** e **Firebase Crashlytics** (Google). Ela só é habilitada após você consentir explicitamente por meio de um diálogo claro exibido no primeiro uso do app (ou depois, pelo menu de configurações). O objetivo dessa telemetria é entender uso do app, estabilidade e desempenho, e não perfilar pessoas.

Nenhum dado é coletado antes do seu consentimento: a coleta de telemetria fica desativada por padrão no app e só é ativada em resposta ao seu consentimento explícito.

Quando habilitada, a telemetria pode incluir informações agregadas, como:
* Versão do app;
* Versão do Android;
* Informações básicas de plataforma/compatibilidade;
* Dados anônimos de falhas e erros (via Firebase Crashlytics);
* Eventos de uso de recursos (por exemplo: "grupo criado", "partida finalizada", "times rebalanceados", "rebalanceamento por sequência de vitórias", "backup/CSV exportado ou importado"), sem identificadores pessoais ou do grupo.

Esses dados não incluem nomes de jogadores, detalhes de partidas, nomes de grupos, contatos ou qualquer conteúdo bruto inserido no app. Eles não são usados para identificar você pessoalmente. Você pode revogar o consentimento a qualquer momento pelo menu de configurações do app (o mesmo botão usado para ativar), e a coleta de telemetria é interrompida imediatamente assim que o consentimento é retirado.

### 3. Armazenamento Local
Todos os dados principais do app são armazenados localmente no seu dispositivo por meio do banco interno (**Room/SQLite**). Nenhum dado do uso base do app é enviado a servidores externos de forma rotineira.

### 4. Login, Sincronização Premium em Nuvem e Transmissão ao Vivo para Espectadores
O app oferece login opcional e recursos premium que vão além da experiência somente local descrita acima:
* **Login**: você pode criar uma conta gratuita (e-mail/senha ou login com o Google) ou, no caso de espectadores, entrar em um grupo de forma anônima com um código de convite, tudo por meio do **Firebase Authentication**.
* **Sincronização premium em nuvem**: uma assinatura paga permite que o organizador do grupo sincronize os dados do grupo — jogadores, placar ao vivo, histórico de partidas e registros de Elo — entre dispositivos, usando **Firebase/Firestore**.
* **Transmissão ao vivo para espectadores**: o organizador pode compartilhar um código de convite/entrada para que espectadores acompanhem o placar em tempo real e, caso o organizador habilite, também o histórico de partidas e o Elo dos jogadores — sem que o espectador precise criar uma conta completa.
* **Personalização de cores dos times**: assinantes podem personalizar as cores de cada time no próprio aparelho; o organizador de um grupo sincronizado também pode definir cores que valem para todos que visualizam aquele grupo.

Esses recursos são opcionais e separados da experiência principal do app, que é local. Os dados da conta podem incluir seu e-mail, nome de exibição e um identificador único de conta usado para autenticação e controle de acesso, além dos dados de grupo sincronizados descritos acima.

### 5. Assinaturas e Pagamentos
As assinaturas Premium (planos mensal e anual) são compradas e gerenciadas por meio do **Google Play Billing**. Os preços são definidos e exibidos pelo Google Play no momento da compra e podem variar por país/região e impostos aplicáveis. Os dados de pagamento (como informações de cartão) são processados inteiramente pelo Google e nunca são coletados ou armazenados pelo app ou pelo desenvolvedor. As assinaturas se renovam automaticamente até serem canceladas e podem ser revisadas, alteradas ou canceladas a qualquer momento na página de assinaturas da Google Play Store. Reembolsos seguem as políticas próprias do Google Play.

### 6. Compartilhamento de Dados
O aplicativo não vende seus dados e não compartilha seus dados locais com terceiros como parte do produto base.

Se você usar a sincronização em nuvem ou a transmissão ao vivo para espectadores, os dados de grupo correspondentes (jogadores, placar ao vivo, histórico de partidas, registros de Elo) ficam armazenados no Firebase/Firestore e disponíveis apenas para os dispositivos/contas aos quais você conceder acesso explicitamente — membros do grupo e espectadores com um código de convite válido. Os dados de assinatura e pagamento são tratados pelo Google Play Billing conforme a política de privacidade do próprio Google, e a telemetria (Seção 2) é compartilhada apenas conforme descrito ali.

### 7. Serviços de Terceiros
O app utiliza **Firebase Authentication**, **Firebase Firestore** e **Firebase Cloud Functions** (Google) para viabilizar login, sincronização premium em nuvem e transmissão ao vivo para espectadores, além do **Google Play Billing** (Google) para processar as compras de assinatura. Se você optar pela funcionalidade opcional de telemetria anônima descrita na Seção 2, o app também utiliza **Firebase Analytics** e **Firebase Crashlytics**. O app não utiliza nenhum outro serviço de analytics, rede de anúncios ou rastreamento comportamental.

### 8. Permissões
O aplicativo solicita a permissão de INTERNET, usada para sincronizar dados com o Firebase quando você utiliza os recursos opcionais de login, sincronização premium em nuvem ou transmissão ao vivo, e para enviar a telemetria anônima descrita na Seção 2 quando você consente com ela (nenhuma requisição de rede é feita para essas finalidades caso você não utilize esses recursos). O app não solicita outras permissões especiais do dispositivo, como câmera, microfone ou localização.

### 9. Exclusão de Dados
Você controla seus dados locais:
* É possível excluir jogadores, grupos ou histórico de partidas diretamente no app.
* A desinstalação do aplicativo remove permanentemente todos os dados armazenados localmente.

Se a sincronização premium em nuvem for usada, excluir um grupo sincronizado no app também apaga os dados desse grupo na nuvem (jogadores, placar ao vivo, histórico de partidas, registros de Elo, registros de membros/espectadores e códigos de convite) dos nossos servidores — essa ação não pode ser desfeita. Excluir sua conta também apaga o registro da sua conta e todos os grupos em nuvem dos quais você é proprietário, mesmo que o app não tenha conseguido confirmar a exclusão antes de a conta ser removida. Cancelar uma assinatura interrompe as cobranças futuras, mas não apaga, por si só, seus dados em nuvem — use a exclusão de grupo ou de conta pelo app para isso.

### 10. Crianças
O aplicativo não é direcionado a menores de 13 anos e não coleta intencionalmente dados pessoais de crianças.

### 11. Alterações
Esta política pode ser atualizada em versões futuras para refletir mudanças no app ou requisitos legais. Alterações significativas serão comunicadas nas notas de atualização e em avisos do próprio aplicativo, quando aplicável.

### 12. Contato
Dúvidas sobre esta política podem ser enviadas para o desenvolvedor por meio da página oficial do app na **Google Play Store** ou pelo repositório GitHub associado ao projeto.

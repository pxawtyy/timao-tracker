# 🦅 TimãoTracker — O Aplicativo do Bando de Loucos 🏴🏳️

Aplicativo mobile Android desenvolvido em **Kotlin** e **Jetpack Compose** para atender a uma necessidade cotidiana do torcedor corinthiano: gerenciar promessas para São Jorge, controlar gastos com o Coringão (ingressos, caravanas, cerveja), consultar o clima e sensação térmica em tempo real na **Neo Química Arena (Itaquera)** para não passar perrengue no estádio, programar alarmes automáticos de dia de jogo e salvar o perfil personalizado da Fiel Torcida.

Visual propositalmente fanático, raiz e extravagante (preto, branco, toques de ouro do mundial e sangue de raça), repleto de homenagens à Fiel, ao Craque Neto e ao Sport Club Corinthians Paulista.

---

## 📋 Requisitos Obrigatórios Atendidos

| Requisito | Como foi implementado |
| :--- | :--- |
| **Navegação entre Telas** | Barra de navegação inferior (`NavigationBar` / `NavigationBarItem`) com 4 telas completas: **Arena** (Home & Clima), **Promessas** (SQLite), **Alarme** (Notificações) e **Fiel** (Perfil & DataStore). |
| **DataStore** | `TimaoDataStore` utilizando `androidx.datastore:datastore-preferences` para persistir o nome do torcedor, torcida organizada favorita, modo *"Craque Neto Pistola"* e frases de guerra da Fiel. |
| **SQLite** | `TimaoDbHelper` gerenciando o banco local `timao_loucura.db` e a tabela `registros_fiel` para cadastrar, listar, alternar status (pago/pendente), excluir e somar os totais de gastos e promessas. |
| **API REST** | `ClimaItaqueraRepository` consumindo de forma assíncrona (`Dispatchers.IO`) a API meteorológica em tempo real da Open-Meteo com as coordenadas exatas da **Neo Química Arena (-23.5453, -46.4742)**. |
| **Notificações Locais & Alarmes** | `TimaoNotificationHelper` + `TimaoAlarmReceiver` utilizando canal de alta prioridade (`timao_channel_fiel`) e `AlarmManager.setExactAndAllowWhileIdle()` para despertar no horário programado mesmo com o app fechado ou tela bloqueada. |

---

## 🗂️ Arquitetura dos Arquivos Criados

- [`app/src/main/java/com/timao/tracker/MainActivity.kt`](app/src/main/java/com/timao/tracker/MainActivity.kt): Interface declarativa em Jetpack Compose com as 4 telas temáticas, totalizadores, cards listrados e formulários.
- [`app/src/main/java/com/timao/tracker/TimaoDbHelper.kt`](app/src/main/java/com/timao/tracker/TimaoDbHelper.kt): Persistência em banco SQLite de promessas e gastos.
- [`app/src/main/java/com/timao/tracker/TimaoDataStore.kt`](app/src/main/java/com/timao/tracker/TimaoDataStore.kt): Persistência de preferências do torcedor no Jetpack DataStore Preferences.
- [`app/src/main/java/com/timao/tracker/ClimaItaqueraRepository.kt`](app/src/main/java/com/timao/tracker/ClimaItaqueraRepository.kt): Requisição HTTP assíncrona à API REST para obter a temperatura e umidade da Arena Itaquera com as recomendações de roupa do Craque Neto.
- [`app/src/main/java/com/timao/tracker/TimaoNotificationHelper.kt`](app/src/main/java/com/timao/tracker/TimaoNotificationHelper.kt): Gerenciamento do canal de notificações, disparos imediatos e agendamento de alarmes exatos com `AlarmManager`.
- [`app/src/main/res/drawable/ic_corinthians_escudo.xml`](app/src/main/res/drawable/ic_corinthians_escudo.xml): Vetor gráfico estilizado com âncora vermelha, remos cruzados e bóia náutica do Timão.
- [`app/src/main/res/drawable/ic_estrela_mundial.xml`](app/src/main/res/drawable/ic_estrela_mundial.xml): Vetor das estrelas douradas dos Mundiais da FIFA (2000 e 2012).
- [`app/src/main/res/drawable/ic_craque_neto.xml`](app/src/main/res/drawable/ic_craque_neto.xml): Arte em vetor do Craque Neto esbravejando *"CÊ É LOUCO PÃO DE QUEIJO"*.
- [`app/src/main/AndroidManifest.xml`](app/src/main/AndroidManifest.xml): Permissões de `INTERNET`, `POST_NOTIFICATIONS` e `SCHEDULE_EXACT_ALARM`, além do registro do `TimaoAlarmReceiver`.

---

## 🚀 Como Executar

1. Abra o projeto no **Android Studio**.
2. Conecte um emulador Android ou aparelho físico (com depuração USB ativa).
3. Execute o app normalmente pelo botão **Run 'app'** (`Shift + F10`).
4. O APK compilado de debug também é gerado diretamente em `app/build/outputs/apk/debug/app-debug.apk`.

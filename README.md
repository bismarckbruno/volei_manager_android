# Volley Manager 🏐

**Volley Manager** is an Android app built with **Jetpack Compose** and **Room Database** to manage recreational volleyball matches, automate team balancing, track player performance (Elo rating), and keep player rotation fair.

## ✨ Features

### 🎮 Match Management
- **Smart Automatic Team Draw**: The app selects players and balances teams as fairly as possible, mixing participants with different skill levels (using Elo rating) and evenly distributing priority players.
- **Fair Rotation**: Smart waiting queue logic to make sure everyone gets play time.
  - **Match-Based Priority**: When deciding who enters the court or who stays after a loss, **the app prioritizes players who have played fewer matches**.
  - **Streak Handling by Mode**: When the streak limit is reached, the app applies the selected balancing mode: in **Rebalance**, winners are split; in **Rest**, winners may rotate out so waiting teams can play ("King of the Court").
- **Manual Setup**: Dedicated screen to manually choose or adjust team composition.
- **Live Scoreboard**: Real-time score tracking during matches.

### 📊 Ranking and Stats
- **Elo System**: Dynamic rating calculated after every match based on opponent strength (K=32, initial Elo 1200).
- **Match History**: Full game log with teams, final score, Elo deltas, and team Elo averages.
- **Sharing**: Export match history as an image for social sharing.

### 👥 Players and Group Management
- **Multiple Groups**: Create and manage independent groups (e.g., "Tuesday Volleyball", "Beach Volleyball"), each with its own players, history, and settings.
- **Player Profile**: Name, Elo, and priority flag.
- **Priority Player (`isPriority`)**: Generic balancing flag that can represent setters, gender balance, skill-tier distribution, or any group-defined criterion.
- **Late Arrival Toll (`dailyToll`)**: Players who arrive late receive extra virtual games calculated from the average games played by present players.
- **Backup and Restore**: Export/import complete data (JSON) or specific tables (CSV).

### 🎨 Customization
- **Theme**: Light, Dark, or System.
- **Optional Display**: Toggle Elo and Late Arrival Toll visibility in UI.

## 🛠 Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM — all business logic in `VoleiViewModel`; manual DI via `ViewModelFactory` (no Hilt/Dagger)
- **Navigation**: Custom enum-based navigation (`Screen.GAME`, `HISTORY`, `FAQ`, `ABOUT`) with `AnimatedContent`
- **Local Database**: Room (SQLite) with incremental migrations
- **Async**: Coroutines & Flow (`viewModelScope`, `Dispatchers.IO`)
- **JSON Serialization**: Gson 2.10.1
- **Annotation Processing**: KSP (Kotlin Symbol Processing)

## 🚀 Running the Project
1. Clone the repository:
   ```bash
   git clone https://github.com/bismarckbruno/volei_manager_android.git
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and run the app on an emulator or physical device (Android 7.0+ / API 24+).
4. Current Play-targeted SDK levels: **compileSdk 36** and **targetSdk 36** (Android 16).

## ⚙️ Group-Level Rules
- **Team Size**: From 2 to 6 players per side.
- **Victory Limit**: Max consecutive wins before the app applies the configured streak rule (split in Rebalance mode, rest rotation in Rest mode).
- **Priority Distribution**: Ensures at least one priority player per team in automatic balancing (when available).

## 🤝 Contributing and Feedback
Contributions are welcome! Feel free to open a pull request.

Found an issue or have an idea? Open an [Issue here](https://github.com/bismarckbruno/volei_manager_android/issues/new/choose).

## ⚖️ Legal Documentation
- [Privacy Policy (EN-US)](https://bismarckbruno.github.io/volei_manager_android/PRIVACY_POLICY)
- [Terms of Use (EN-US)](https://bismarckbruno.github.io/volei_manager_android/TERMS_OF_USE)
- [Privacy Policy (PT-BR)](https://bismarckbruno.github.io/volei_manager_android/PRIVACY_POLICY_PT_BR)
- [Terms of Use (PT-BR)](https://bismarckbruno.github.io/volei_manager_android/TERMS_OF_USE_PT_BR)
- [Privacy Policy (ES-419)](https://bismarckbruno.github.io/volei_manager_android/PRIVACY_POLICY_ES_419)
- [Terms of Use (ES-419)](https://bismarckbruno.github.io/volei_manager_android/TERMS_OF_USE_ES_419)
- [MIT License](LICENSE)

## ☕ Support the Project

**Volley Manager** is a free independent project. If the app helps you organize better matches and you want to support future development, consider buying me a coffee.

### Ways to support:

* **GitHub Sponsors:** [Support via GitHub](https://github.com/sponsors/bismarckbruno)
* **PIX:** See options below:

<details>
  <summary><b>Click to show PIX QR code and key</b></summary>
  <br>
  <div align="center">
    <img src="apoio/qr_code_pix.png" width="200" alt="PIX QR Code"><br>
    <sub>Scan the QR code above or use the copy-paste code below:</sub>
    <br><br>
    <p><code>00020126650014br.gov.bcb.pix0136d143999e-2f7a-4ce4-84c3-b3b03b41536e0203Pix5204000053039865802BR5925BRUNO_BISMARCK_DA_SILVA_M6006CAXIAS62210517ApoioVoleiManager63044F13</code></p>
  </div>
</details>

---
*Any amount helps keep both coffee and code flowing!* 🏐

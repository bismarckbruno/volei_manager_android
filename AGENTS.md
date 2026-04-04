# AGENTS.md — Vôlei Manager

Android app (Kotlin + Jetpack Compose + Room) for managing recreational volleyball matches.  
Package: `com.bismarck.voleimanager` · Min SDK 24 · Target SDK 34

---

## Architecture

**MVVM with manual DI** — no Hilt/Dagger. The entire DI chain is wired in `MainActivity`:

```
AppDatabase.getDatabase(context)
  → VoleiRepository(dao)
    → VoleiViewModel(application, repository)   [via VoleiViewModelFactory]
```

**Single ViewModel** (`ui/viewmodel/VoleiViewModel.kt`) holds all runtime state as `StateFlow`/`MutableStateFlow`. There is no other ViewModel.

**Navigation** is custom enum-based (no `NavController`):
```kotlin
enum class Screen { GAME, HISTORY, FAQ, ABOUT }
viewModel.navigateTo(Screen.HISTORY)
```
Screen switching is handled inside `VoleiManagerApp.kt` via `AnimatedContent`.

---

## Key Source Layout

| Path | Purpose |
|------|---------|
| `data/model/` | Room entities: `Player`, `MatchHistory`, `GroupConfig`, `PlayerEloLog` |
| `data/VoleiDao.kt` | Single DAO for all tables |
| `data/VoleiRepository.kt` | Thin wrapper over DAO; exposes Flow properties + suspend funs |
| `ui/viewmodel/VoleiViewModel.kt` | All business logic, game state, import/export |
| `ui/VoleiManagerApp.kt` | Root composable, nav drawer, dialogs, screen routing |
| `ui/game/GameScreen.kt` | Game screen UI (~1100 lines) |
| `ui/AppScreens.kt` | History, FAQ, About screens |
| `ui/theme/Theme.kt` | Material 3 theme + `LocalExtendedColors` custom CompositionLocal |
| `util/EloCalculator.kt` | Elo delta: K=32, team-average-based |
| `util/TeamBalancer.kt` | Utility (less used — main balancing is inline in ViewModel) |

---

## Data & Group Scoping

Every entity uses `groupName: String` as a soft foreign key. The ViewModel filters global flows per the active group:

```kotlin
val currentGroupPlayers = combine(players, _currentGroupConfig) { list, config ->
    list.filter { it.groupName == config.groupName }
}
```

When adding a new entity, always pass `groupName` explicitly. Renaming/deleting a group cascades manually via `VoleiRepository.renameGroup` / `deleteGroup`.

---

## Domain Concepts

- **`isPriority`** (`Player`) — a generic "distribute evenly" flag. The app guarantees at least one priority player per team before filling remaining slots. Each group defines its own meaning: some use it for setters (levantadores), others to ensure gender balance (e.g. at least one woman per team), others to spread players of a known skill tier. The label is intentionally neutral in the data model.
- **`dailyToll` / `tollDate`** (`Player`) — late-arrival penalty (pedágio). Calculated as the average games played by already-present players at the moment a new player checks in. Resets daily (compared against `yyyy-MM-dd`).
- **Elo** — default 1200.0. Delta = `EloCalculator.calculateEloChange(winnerAvgElo, loserAvgElo)`.  Each match logs one `PlayerEloLog` entry per player for chart history.
- **Streak / victoryLimit** — tracked in `_currentStreak` / `_streakOwner`. When `currentStreak >= config.victoryLimit`, winning team is split in `startNextRound()`.
- **`MatchHistory.teamA/teamB`** — comma-separated player names (sorted alphabetically), not IDs.

---

## Database Migrations

DB version is currently **10** (`AppDatabase.kt`). `exportSchema = false`.  
When adding columns, add a `Migration(old, new)` object and register it in `addMigrations(...)`.  
`fallbackToDestructiveMigration()` is enabled as a safety net — avoid relying on it for release.

---

## Theme & Colors

- `AppTheme` supports dynamic color (Android 12+), plus custom light/dark/contrast schemes.
- Team B cards use `LocalExtendedColors.current.anotherPrime` (a custom `ColorFamily` provided via `CompositionLocalProvider`). Always use this instead of hardcoding for Team B color.
- History share bitmap rendering disables dynamic color (`dynamicColor = false`) to guarantee brand colors.

---

## Preferences

User settings are persisted in `SharedPreferences("volei")` directly from the ViewModel (no DataStore):

| Key | Type | Default |
|-----|------|---------|
| `theme` | String (`ThemeMode` enum name) | `SYSTEM` |
| `show_elo` | Boolean | false |
| `show_toll` | Boolean | false |
| `is_supporter` | Boolean | false |
| `team_color` | String (`TeamColorTheme` enum name) | `DEFAULT` |

---

## Build & Run

- **Build system**: Gradle KTS + Version Catalog at `gradle/libs.versions.toml`
- **Annotation processing**: KSP (not kapt) — Room compiler is `ksp(libs.androidx.room.compiler)`
- **Run**: Open in Android Studio → Sync Gradle → Run on emulator or device (Android 7.0+)
- **No CI scripts** or test commands beyond the default `./gradlew test` / `./gradlew connectedAndroidTest`

---

## Patterns to Follow

- All DB writes go through `VoleiRepository`; never access `VoleiDao` directly from the UI layer.
- Launch coroutines from `viewModelScope`; use `Dispatchers.IO` for DB/file operations.
- Dialog visibility is managed by local `var show* by remember { mutableStateOf(false) }` in `VoleiManagerApp`.
- Export/import uses `FileProvider` (`${context.packageName}.fileprovider`) — see `AndroidManifest.xml` for the provider declaration.
- CSV parsing uses the custom `smartSplit()` function (handles quoted commas); don't use `.split(",")` on raw CSV lines.


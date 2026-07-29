# MedDiktat

MVP einer Android-App (Kotlin, Jetpack Compose) für **medizinische Diktate** mit
Fokus auf **lokale Speicherung, Datenschutz (Privacy by Design)** und **saubere,
erweiterbare Architektur**. Keine Cloud, keine Tracker, keine Analytics, keine
automatische Synchronisation.

## Build & Start

Das Projekt ist ein Standard-Gradle-Android-Projekt. Der Gradle-Wrapper ist
enthalten (Gradle 8.13), ein separat installiertes Gradle wird nicht benötigt.

1. In **Android Studio** (Ladybug / AGP 8.7+) öffnen – oder auf der
   Kommandozeile `./gradlew :app:assembleDebug` ausführen.
2. Auf Gerät/Emulator mit **API 26+** (Android 8.0) ausführen.

Zum Kompilieren wird ein vollständiges **JDK 17** benötigt; eine JRE genügt
nicht, da Gradle eine Toolchain mit `javac` verlangt.

Toolchain: Kotlin 2.0.20 · AGP 8.7.3 · compileSdk/targetSdk 35 · minSdk 26 ·
Room 2.6.1 · Hilt 2.52 · Material 3 · JDK 17.

### Release-Build und Signierung

`./gradlew :app:assembleRelease` aktiviert R8 (Shrinking + Obfuskierung). Die
Signierung ist optional konfiguriert:

- **Ohne Zugangsdaten** entsteht `app-release-unsigned.apk`. Der Build schlägt
  also nicht fehl, wenn jemand das Repository ohne Schlüssel klont.
- **Mit Zugangsdaten** entsteht das signierte `app-release.apk`.

Dazu `keystore.properties.example` nach `keystore.properties` kopieren und
ausfüllen. Diese Datei sowie `*.jks` / `*.keystore` sind per `.gitignore`
ausgeschlossen und dürfen **nicht** eingecheckt werden. Alternativ lassen sich
die Werte über die Umgebungsvariablen `MEDDIKTAT_STORE_FILE`,
`MEDDIKTAT_STORE_PASSWORD`, `MEDDIKTAT_KEY_ALIAS` und `MEDDIKTAT_KEY_PASSWORD`
setzen, etwa in einer CI.

Keystore erzeugen (Passwörter werden interaktiv erfragt und landen damit nicht
in der Shell-History):

```
keytool -genkeypair -v -keystore meddiktat-release.jks \
  -alias meddiktat -keyalg RSA -keysize 4096 -validity 10000
```

Der Keystore ist unersetzlich: geht er verloren, lässt sich eine bereits
veröffentlichte App nicht mehr mit Updates versorgen. Getrennt vom Projekt und
gesichert aufbewahren.

## Architekturübersicht

Saubere Schichtung nach **MVVM + Repository Pattern**, Abhängigkeiten zeigen
immer nur nach innen Richtung `domain`:

```
ui  ─────────────▶  domain  ◀─────────────  data / recorder / playback / export
(Compose+VM)        (Modelle + Interfaces)   (Android-Implementierungen)
        └──────────────── Hilt (di) verdrahtet alles ────────────────┘
```

- **`domain`** – reine Kotlin-Modelle und Schnittstellen (`DictationRepository`,
  `AudioRecorder`, `AudioPlayer`, `SpeechToTextEngine`, `UploadService`). Kennt
  weder Android noch Room. Das ist die Stelle, an der Erweiterbarkeit „eingebaut“ ist.
- **`data`** – Room (`AppDatabase`, `DictationDao`, `DictationEntity` + Mapper),
  `DictationRepositoryImpl`, Dateiverwaltung (`DictationFileManager`), sowie die
  MVP-Stubs `NoopUploadService` und `NoopSpeechToTextEngine`.
- **`recorder` / `playback`** – `MediaRecorder`- bzw. `MediaPlayer`-Implementierungen
  der Domain-Abstraktionen.
- **`export`** – `ExportManager` baut den FileProvider-Teilen-Intent.
- **`ui`** – Compose-Screens (`list`, `record`, `detail`), ViewModels, Theme,
  Navigation, gemeinsame Komponenten.
- **`di`** – vier Hilt-Module (Database, Repository, Audio, Service).

### Datenfluss (Beispiel Aufnahme)
`RecordScreen` → `RecordViewModel.start()` → `AudioRecorder` (MediaRecorder)
schreibt `.m4a` in `filesDir/dictations` → bei Stop erzeugt das ViewModel ein
`Dictation`-Modell → `DictationRepository.upsert()` → Room. Die Liste beobachtet
Room via `Flow` und aktualisiert sich automatisch.

## Datenschutz / Privacy by Design

- Audiodateien liegen **ausschließlich** in `context.filesDir/dictations`
  (App-Sandbox, für andere Apps nicht lesbar). Keine öffentlichen Ordner.
- **Nur `RECORD_AUDIO`** als Berechtigung. Keine Speicherberechtigungen.
- **Backup deaktiviert** (`allowBackup=false` + `data_extraction_rules`), damit
  Diktate nicht über Cloud-Backup/Geräte-Transfer das Gerät verlassen.
- `MediaRecorder` ist ab API 30 als `isPrivacySensitive` markiert.
- Dateinamen ohne Klarnamen: `yyyy-MM-dd_HH-mm-ss_dictation_<randomId>.m4a`.
- **Export nur als bewusste Nutzeraktion** über `FileProvider` (temporäre,
  widerrufbare content://-URI) und erst nach Bestätigung eines Warnhinweises.
- Wiedergabe ausschließlich in-App (kein externer Player-Intent).

## Datenmodell

`domain/model/Dictation.kt`: `id, createdAt, updatedAt, filename, displayTitle,
audioPath, durationMs, recordingDate, status, priority?, dictationType?,
caseReference?, note?, transcript?, exportState`.
Status: `NEW · REVIEWED · EXPORTED · ARCHIVED`.

## Hinweise zu späteren Erweiterungen

Die Architektur ist bewusst auf diese Ausbaustufen vorbereitet:

- **PIN-/Biometrie-Schutz** – neue `ui`-Route + einfacher Auth-Gate vor dem NavHost.
- **Verschlüsselung**
  - Metadaten: Room mit SQLCipher öffnen (`SupportFactory` in `DatabaseModule`).
  - Audiodateien: transparente Verschlüsselungsschicht im `DictationFileManager`
    (z. B. Jetpack Security / `EncryptedFile`).
- **Gesicherter Server-Upload** – echte Implementierung von `UploadService`
  (TLS, Auth, serverseitige Verschlüsselung) und Austausch der Bindung in
  `di/ServiceModule`. `ExportState.UPLOADED` ist bereits vorgesehen.
- **Offline-Transkription** – echte `SpeechToTextEngine` (z. B. Vosk/Whisper on-device);
  `isAvailable` wirkt als Feature-Flag, `transcript` ist im Modell vorhanden.
- **Schreibkraft-Workflow** – zusätzliche Status/Übergabelogik auf Basis von
  `DictationStatus` und `caseReference`.

Jede dieser Erweiterungen berührt idealerweise nur eine Schicht bzw. eine
DI-Bindung, nicht die restliche App.

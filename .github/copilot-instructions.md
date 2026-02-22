# Copilot instructions for MiMascota

## Big picture
- Single-module Android app (module app) using Jetpack Compose + Material3. Most app logic, data models, DAOs, and UI screens live in one file: [app/src/main/java/com/example/mimascota/MainActivity.kt](app/src/main/java/com/example/mimascota/MainActivity.kt).
- Theme setup is in [app/src/main/java/com/example/mimascota/ui/theme/Theme.kt](app/src/main/java/com/example/mimascota/ui/theme/Theme.kt).
- App entry is MainActivity with Compose content; navigation is Compose Navigation via `AppNavHost` in the same file.

## Data layer patterns
- Room is used directly in UI: entities/DAOs and `MascotaDatabase` are defined in [app/src/main/java/com/example/mimascota/MainActivity.kt](app/src/main/java/com/example/mimascota/MainActivity.kt).
- Database name is "mascotas-db"; built with `fallbackToDestructiveMigration()` and `allowMainThreadQueries()` in `MainActivity` and again inside `AgregarRecordatorioScreen`.
- UI state typically calls DAO methods directly and stores results in `remember { mutableStateOf(...) }` lists, then refreshes after insert/delete.

## Navigation & UI conventions
- Route strings are hardcoded and passed with IDs, e.g. `historial/{id}`, `recordatorios/{id}`, `agregar_mascota`, `agregar_recordatorio/{id}` in [app/src/main/java/com/example/mimascota/MainActivity.kt](app/src/main/java/com/example/mimascota/MainActivity.kt).
- UI text is Spanish, with form validation errors shown via `AnimatedVisibility` + `Text` (see multiple screens in MainActivity).
- Images are loaded via Coil `AsyncImage` and via `painterResource` for drawable backgrounds.

## Notifications & integrations
- Reminder notifications are polled in a coroutine loop inside `AppNavHost` (checks every 10 seconds). It compares recordatorio `fecha` and `hora` strings formatted as dd/MM/yyyy and HH:mm in [app/src/main/java/com/example/mimascota/MainActivity.kt](app/src/main/java/com/example/mimascota/MainActivity.kt).
- WhatsApp sharing is implemented by building a text intent in `HistorialMedicoScreen`.
- Manifest declares POST_NOTIFICATIONS in [app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml).

## Build & test workflow
- Gradle wrapper is used. Common tasks: assembleDebug, installDebug, test, connectedAndroidTest.
- Compile/target SDK 36, minSdk 30, Java/Kotlin 17, Compose plugin enabled. Versions are in [gradle/libs.versions.toml](gradle/libs.versions.toml) and dependencies in [app/build.gradle.kts](app/build.gradle.kts).

## When adding features
- Follow the existing pattern of placing new Composables, DAO calls, and simple validation directly in [app/src/main/java/com/example/mimascota/MainActivity.kt](app/src/main/java/com/example/mimascota/MainActivity.kt) unless you are refactoring.
- Keep date/time string formats consistent with current recordatorios logic (dd/MM/yyyy and HH:mm) to avoid missed matches.

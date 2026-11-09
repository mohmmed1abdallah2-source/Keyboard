# Iteration 1 — Offline-first Android keyboard

## Scope
Build a Kotlin Android input method with a custom Canvas keyboard, Arabic/English layouts, symbol and number layers, repeat backspace, cursor trackpad, and long-press alternate characters.

## Repository workflow
- Bootstrap commit: `main` contains only the repository README.
- All Iteration 1 implementation changes are isolated on `feature/iteration-1-keyboard-engine`.
- Do not merge to `main` without explicit owner approval.

## Build environment
- JDK 17
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- Gradle 8.9
- compileSdk 35, minSdk 23
- GitHub Actions generates Gradle Wrapper if it is absent, runs `clean assembleDebug`, and uploads the APK.

## Implemented files
The app module, manifest/IME declaration, programmatic setup activity, Canvas view, layout definitions, and IME service are tracked in this branch.

## Validation record
| Check | Result |
|---|---|
| Repository bootstrap | Completed |
| Feature branch creation | Completed |
| Source implementation | In progress |
| Gradle build | Not yet run |
| APK artifact | Not yet produced |

## Engineering notes
- The keyboard UI is drawn with Android Canvas and Paint; no Compose or Button widgets are used.
- MainActivity is built programmatically and includes the two IME setup actions and a text field.
- The keyboard performs input locally and declares no network permission.
- Performance goals (under 10 ms input response and under 30 MB RAM) are targets, not claimed measurements; they require runtime profiling on representative devices.

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
| Source implementation | Completed on feature branch |
| Gradle build | Triggered by GitHub Actions; latest run pending verification |
| APK artifact | Not yet verified or downloaded |

## Stage 1 implementation record\n- Added root Gradle settings, root plugin declarations, module Gradle configuration, and JVM/AndroidX properties.\n- Added GitHub Actions workflow with JDK 17, Android SDK platform/build-tools installation, Gradle 8.9 provisioning, conditional wrapper generation, `assembleDebug`, and APK artifact upload.\n- Added the IME service declaration and Arabic/English subtypes.\n- Added programmatic setup activity with enable/picker actions and an editable test field.\n- Added four keyboard modes (Arabic, English lowercase, English uppercase, and symbols with Western and Arabic-Indic digits).\n- Added Canvas key rendering, sublabels, long-press alternates, accelerating repeat delete, horizontal spacebar cursor movement, and haptic feedback.\n- Added popup positioning relative to the keyboard view and retained hardware-accelerated Canvas rendering.\n\n## Engineering notes
- The keyboard UI is drawn with Android Canvas and Paint; no Compose or Button widgets are used.
- MainActivity is built programmatically and includes the two IME setup actions and a text field.
- The keyboard performs input locally and declares no network permission.
- Performance goals (under 10 ms input response and under 30 MB RAM) are targets, not claimed measurements; they require runtime profiling on representative devices.

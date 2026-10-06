# Contributing to VoxSign-Android

Thanks for your interest in VoxSign! This document describes how to propose changes.

## Sign your work (DCO)

Every commit must be signed off. Use:

```
git commit -s -m "Your message"
```

The sign-off certifies you have the right to submit the code, per the
[Developer Certificate of Origin](https://developercertificate.org/). CI rejects
commits that are missing the `Signed-off-by` trailer.

## Development setup

1. Install JDK 17 and the Android SDK (platform 35, build-tools 35.0.0).
2. Copy `local.properties.example` semantics into `local.properties` pointing at your SDK:
   ```
   sdk.dir=/path/to/Android/sdk
   ```
   (`local.properties` is git-ignored.)
3. Build and test:
   ```
   ./gradlew assembleDebug
   ./gradlew lint
   ```

## Code style

- Kotlin with Jetpack Compose; follow the existing package layout under `app/src/main/java/ai/voxsign/android/`.
- All user-facing strings live in `res/values/strings.xml` (English). Add translations under `res/values-*/`.
- Keep behaviour aligned with the iOS client where a feature exists there.

## Pull requests

- Keep PRs focused and describe the user-visible change.
- CI must pass: `assembleDebug`, `lint`, the sensitive-info scan, and the DCO check.
- At least one CODEOWNER approval is required before merge.

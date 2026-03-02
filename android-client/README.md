# Android Client (Jetpack Compose)

This directory contains the Android client source layout and Compose UI implementation for the mindful barrier flow.

## What is implemented
- `MainActivity` entry point launching Compose UI.
- `MindfulViewModel` integrating with shared core mechanics (`MindfulnessEngine`).
- `MindfulApp` composable demonstrating barrier flow and progress updates.
- `LaunchMonitor` interface for platform-specific foreground app detection integration.

## Notes
In this environment, CI focuses on `:core` and `:backend` JVM modules (fully built and tested).
The Android project scaffolding is provided as source architecture and can be integrated with AGP/SDK setup in Android Studio.

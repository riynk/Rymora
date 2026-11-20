# Rymora

Rymora is an offline-first Android music player with a retro-inspired interface.

## Project baseline

- Native Android application
- Kotlin + Jetpack Compose + Material 3
- Minimum Android version: Android 12 (API 31)
- Media3 ExoPlayer + MediaSessionService for one shared playback engine
- Mini Player is a persistent UI component, not a navigation destination
- Now Playing is a dedicated full-screen destination
- Room is reserved for playlists, favorites, and play history
- DataStore is reserved for app preferences

## Navigation

Home · Library · Search · Playlist · Settings

Now Playing is opened from the Mini Player and is intentionally excluded from the bottom navigation.

## Animation direction

- Spring-based motion for interactive controls
- Album/record rotation in Now Playing (currently a visual scaffold)
- Shared Mini Player → Now Playing artwork transition (planned)
- Audio-reactive visualizer (planned; requires Media3 audio session integration)
- Neon and album-derived background motion (planned)

## Build

Open this repository in the latest stable Android Studio with JDK 17 and allow Gradle sync.
The project uses Gradle Kotlin DSL and a version catalog.

## Current implementation status

This commit establishes the Android project skeleton, app theme, navigation shell, Mini Player UI component, Now Playing scaffold, and Media3 playback service.
The playback controller, device audio library scanner, Room entities/DAOs, and DataStore implementation are the next implementation stage.

# Rymora

Rymora is an offline-first Android music player with a retro-inspired interface.

## Project baseline

- Native Android application using Kotlin + Jetpack Compose + Material 3
- Minimum Android version: Android 12 (API 31)
- Media3 ExoPlayer + MediaSessionService owns one shared playback engine
- Mini Player is a persistent global component, not a navigation destination
- Now Playing is a dedicated full-screen destination
- Room is reserved for playlists, favorites, and play history
- DataStore is reserved for app preferences

## Navigation

Home · Library · Search · Playlist · Settings

Now Playing opens from the Mini Player and is intentionally excluded from bottom navigation.

## Playback implemented

- A MediaController connects the UI to the single ExoPlayer in PlaybackService
- PlaybackViewModel exposes one shared playback state to Mini Player and Now Playing
- MediaStore scans local audio after the user grants audio permission
- Library rows play a selected song and queue the scanned local collection
- Mini Player supports open, play/pause, and next
- Now Playing supports play/pause, previous/next, seeking, and a vinyl rotation animation that runs only during playback

## Animation direction

- Spring-based motion for interactive controls
- Shared Mini Player → Now Playing artwork transition (planned)
- Audio-reactive visualizer (planned; requires Media3 audio session integration)
- Neon and album-derived background motion (planned)

## Build

Open this repository in Android Studio with JDK 17 and allow Gradle sync.
The project uses Gradle Kotlin DSL and a version catalog.

## Remaining work

- Verify Gradle sync/build and test on Android 12+
- Add album artwork rendering and shared-element transition
- Add robust permission-denied and empty-library states
- Add Room persistence for playlists, favorites, and history
- Add DataStore preferences, search, and playlist management
- Add audio visualizer and playback queue controls

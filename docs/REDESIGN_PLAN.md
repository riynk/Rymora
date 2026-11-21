# Rymora Redesign Plan

Target: Android 12+ local/offline-first music player using Kotlin and Jetpack Compose.

## Direction
Rebuild the user experience and app structure around a YouTube Music-inspired flow while retaining Rymora's native Android foundation. This is a design reference, not a copy of proprietary assets or branding.

## Keep / reuse
- Kotlin + Jetpack Compose
- AndroidX Media3 ExoPlayer and MediaSessionService
- MediaStore local audio discovery
- Existing app identity and package namespace (unless a migration is later approved)
- Existing theme/build setup where it remains compatible

## Redesign
- App shell and navigation
- Home / Library / Search / Settings screens
- Persistent Mini Player
- Full-screen Now Playing and gesture transition
- Queue / Up Next
- Playback state ownership and UI state flow
- Component library, typography, spacing, colors, artwork treatment
- ViewModels and feature boundaries as needed

## Target structure
- data: MediaStore data source, repository, models
- playback: service, controller, state
- feature/home
- feature/library
- feature/search
- feature/player (mini player, now playing, queue)
- feature/settings
- navigation
- ui/theme and ui/components

## UX requirements
- All local audio is discovered automatically (respect Android media permissions).
- One playback engine and one MediaSession; UI controls use a controller.
- Mini Player persists across primary navigation when a track is active.
- Tap Mini Player or swipe up opens Now Playing.
- Swipe down from Now Playing collapses to Mini Player from the player surface.
- Play/pause, previous/next, seek, shuffle, repeat and queue state remain synchronized.
- Empty, loading, permission-denied, and no-audio states are designed intentionally.

## Implementation phases
1. Audit current source, build configuration, permissions, and playback flow.
2. Establish navigation shell and design system.
3. Rework library data flow and song browsing.
4. Rework playback controller/service state.
5. Implement Mini Player <-> Now Playing transition and gestures.
6. Queue, search, playlists, settings.
7. Run build/tests and inspect Android 12+ behavior; fix regressions.

## Git workflow
Work on `redesign/ytmusic-inspired`; keep `main` unchanged. Make incremental commits and open a PR for review rather than merging automatically.

## Source reuse
External GitHub code must be reviewed for compatibility and license obligations before copying. Prefer AndroidX/Media3 and small, isolated reusable implementations; retain required attribution and notices.

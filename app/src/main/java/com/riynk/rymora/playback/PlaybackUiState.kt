package com.riynk.rymora.playback

import android.net.Uri

/**
 * Immutable UI-facing snapshot shared by Mini Player and Now Playing.
 * The service remains the owner of the actual ExoPlayer instance.
 */
data class PlaybackUiState(
    val mediaId: String? = null,
    val title: String = "",
    val artist: String = "",
    val artworkUri: Uri? = null,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false
) {
    val hasTrack: Boolean get() = mediaId != null
}

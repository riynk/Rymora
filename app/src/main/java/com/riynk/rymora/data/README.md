# Data layer

Room will own user-created and app-managed data such as playlists, favorites, and play history.
DataStore will own small preferences such as theme, playback options, and library settings.

Keep media playback itself in the Media3 playback service; do not persist transient player state as database truth.

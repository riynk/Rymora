# Rymora Design System — Obsidian Aurora

This design system replaces the legacy Rymora interface. The UI is implemented as native Jetpack Compose screens and components, with Media3 retained as the playback foundation.

## Direction
- Dark-first, music-focused, artwork-led.
- Neutral obsidian surfaces with a restrained aurora-lavender accent; mint and rose are secondary accents.
- Prefer clear hierarchy and generous artwork over stacks of decorative cards.
- Artwork is expressive; all controls and metadata retain strong contrast.

## Color tokens
| Token | Hex | Use |
|---|---|---|
| Background | #09090C | Main app background |
| Surface | #141419 | Sheets, panels and navigation |
| Surface Container | #1D1D24 | Mini player and grouped panels |
| Surface Variant | #292932 | Elevated interactive areas |
| Primary / Aurora | #C9B5FF | Primary action, active state and seek progress |
| Primary Container | #34264F | Subtle accent backgrounds |
| Secondary / Mint | #8BD9C7 | Secondary status and optional accents |
| Secondary Container | #153A33 | Mint accent backgrounds |
| Tertiary / Rose | #FFB8CC | Rare accent |
| Tertiary Container | #492638 | Rose accent backgrounds |
| Text | #F4F1F7 | Primary text |
| Muted | #AAA7B2 | Metadata and supporting text |
| Outline | #45434D | Subtle dividers and borders |
| Error | #FFB4AB | Errors |

Do not use accent colors as large page backgrounds. Artwork may tint the player backdrop, but must not reduce text or control contrast.

## Typography
- Display: 40sp / 32sp, bold, tight tracking.
- Headline: 28sp / 24sp / 20sp, semibold.
- Title: 18sp / 16sp / 14sp, semibold or medium.
- Body: 16sp / 14sp / 12sp.
- Label: 14sp / 12sp / 11sp, medium.
- Track titles use title/headline styles; artist and metadata use muted body styles.
- Use the system font; no bundled font dependency.

## Shapes
- Extra small: 7dp.
- Small: 11dp.
- Medium: 17dp.
- Large: 24dp.
- Extra large: 30dp.
- Artwork is square and clipped to 12–22dp corners.
- Pill shapes are reserved for chips and selected controls.

## Spacing
Use multiples of 4dp: 4, 8, 12, 16, 20, 24, 32, 40, 48.
- Screen edge: 20dp.
- Major sections: 18–24dp.
- Touch targets: minimum 48dp.
- Mini player: 64–72dp plus its progress indicator.
- Bottom navigation accounts for system navigation insets.

## Components
- Home: local-library summary, shuffle-all action and track list.
- Search: searches local song, artist and album metadata.
- Library: Songs, Albums and Artists views over the MediaStore index.
- Mini Player: artwork, title/artist, play-pause, next, progress and swipe-up expansion.
- Now Playing: artwork-led controls, seek/time, queue, shuffle/repeat and swipe-down collapse from the player surface.
- Settings: grouped Appearance, Music Library, Playback and About sections.
- Navigation: Home, Search and Library; selected state uses icon and color.

## Motion and accessibility
- Transitions are short, purposeful and interruptible.
- Keep text contrast high and do not encode state solely with color.
- Respect system font scaling and reduced-motion settings.
- Icon-only controls require semantic descriptions.

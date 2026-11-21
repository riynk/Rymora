# Rymora Design System — Obsidian Aurora

This design system replaces the legacy purple UI. It is the base for the full redesign branch.

## Direction
- Dark-first, music-focused, artwork-led.
- Obsidian surfaces with soft Aurora lavender accent; mint and rose are supporting accents only.
- Avoid excessive cards, outlines, gradients, and ornamental effects.
- Artwork is the expressive element; controls and text remain legible.

## Color tokens
| Token | Hex | Use |
|---|---|---|
| Background | #0B0B0E | Main app background |
| Surface | #18181D | Sheets, panels, mini player |
| Surface Variant | #25252D | Elevated interactive areas |
| Surface Container | #202027 | Grouped settings and secondary panels |
| Primary / Aurora | #D0BCFF | Primary action, active state, seek progress |
| Primary Container | #382A52 | Subtle accent backgrounds |
| Secondary / Mint | #80D8C2 | Secondary status / optional accents |
| Secondary Container | #153B34 | Mint accent backgrounds |
| Tertiary / Rose | #F0B6C7 | Rare accent |
| Tertiary Container | #4A2937 | Rose accent backgrounds |
| Text | #F2F0F5 | Primary text |
| Muted | #B4B1BC | Metadata and supporting text |
| Outline | #45444D | Subtle dividers and borders |
| Error | #FFB4AB | Errors |

Do not use accent colors as large page backgrounds. Artwork-derived colors may tint a player backdrop, but must not replace text/control contrast.

## Typography
- Display: 40sp / 32sp, bold, tight tracking.
- Headline: 28sp / 24sp / 20sp, semibold.
- Title: 18sp / 16sp / 14sp, semibold or medium.
- Body: 16sp / 14sp / 12sp.
- Label: 14sp / 12sp / 11sp, medium.
- Track titles use title/headline styles; artist and metadata use bodySmall/bodyMedium with muted color.
- Use system font initially; no bundled font dependency until visual QA proves one is needed.

## Shapes
- Extra small: 6dp.
- Small: 10dp.
- Medium: 16dp.
- Large: 24dp.
- Extra large: 32dp.
- Artwork is generally square and clipped to 16–24dp corners, depending on size.
- Pill shapes reserved for chips and selected controls.

## Spacing scale
Use multiples of 4dp: 4, 8, 12, 16, 20, 24, 32, 40, 48.
- Screen edge: 20dp (compact width may use 16dp).
- Between section title and content: 12–16dp.
- Between major sections: 24–32dp.
- Touch target: minimum 48dp.
- Mini player height: target 64–72dp excluding its progress indicator.
- Bottom navigation: account for system navigation insets.

## Iconography
- Use Material/Compose icons consistently; avoid mixed stroke weights.
- Icon-only actions need semantic descriptions.
- Standard control sizes: 24dp; prominent player controls 28–32dp.
- Play/pause is the visual priority; secondary actions should not compete.

## Component principles
- Home/library rows: clear title, supporting metadata, artwork where useful, no unnecessary borders.
- Mini player: artwork, title/artist, play-pause, next, thin progress.
- Now Playing: artwork-led; title and artist; seek/time; primary transport; secondary shuffle/repeat/queue.
- Settings: category rows with icon, title, short description, chevron; controls live on their own focused pages.
- Selected navigation is indicated by color and/or filled icon, never color alone.
- Motion should be purposeful, short, and interruptible. Avoid introducing custom motion until the player transition is implemented and tested.

## Accessibility
- Keep text contrast high against surfaces.
- Do not encode state solely with color.
- Respect system font scaling and reduced-motion settings.
- All touch actions should be at least 48dp.

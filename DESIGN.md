# Design System Specification: Night Mission Edition



## 1. Overview & Creative North Star

The Creative North Star for this system is **"Tactical Retro-Futurism."** This is not a standard dark mode; it is a "Night Mission" interface that evokes the high-stakes, low-light environment of a covert operation. By blending a 1980s 8-bit hardware aesthetic with a premium, modern editorial layout, we move away from generic "Material" clones toward a signature visual identity.



The system relies on **Hard-Edged Brutalism**. We reject the soft, rounded corners of modern mobile OSs in favor of a 0px radius (Sharp Corners Only), creating a UI that feels like physical hardware—specifically, a ruggedized tactical cartridge.



## 2. Night Mission Styling Principles

In "Night Mission Mode," the goal is high-contrast legibility and a sense of "glowing" hardware.

- **Deep Immersion:** Backgrounds must use the deep charcoal (`#1A1C1C`) to ground the experience, overlaid with a subtle 16px technical grid to maintain the retro-tech feel.

- **Neon Utility:** The primary accent (`#FF3131`) is used sparingly but aggressively. It represents "active data" or "critical alerts."

- **The Tactile Cartridge:** Components are treated as physical modules. Surface depth is defined by thick, high-contrast borders rather than gradients.



## 3. Colors & Tonal Architecture



### The "No-Line" Rule & The "Tactile" Exception

While standard high-end UI avoids borders, this system uses **intentional, thick structural lines** to mimic hardware casing. However, these are never 1px "dividers." They are either 2px-4px "frame" borders or non-existent, separated by tonal shifts.



| Token | Hex Value | Role |

| :--- | :--- | :--- |

| `background` | `#121414` | The deep void. Base layer for all views. |

| `surface-container-low` | `#1A1C1C` | Secondary background sections / Grid layer. |

| `surface-container` | `#1E2020` | Standard component backing. |

| `surface-container-high`| `#282A2A` | Primary card/module surface. |

| `primary` | `#FFB4AB` | Muted functional red. |

| `primary-container` | `#FF544B` | High-energy "Neon Red" for mission-critical CTAs. |

| `on-surface` | `#E2E2E2` | Primary typography and structural borders. |



### Surface Hierarchy & Nesting

Depth is achieved through "Hardware Stacking."

- **Base Level:** `surface` (#121414) + Grid.

- **Module Level:** `surface-container-high` (#282A2A). This surface must be framed with a 2px `outline` border to feel like a removable cartridge or screen.

- **Active Level:** `primary-container` (#FF544B) for high-importance interaction points.



## 4. Typography: Space Grotesk

We utilize **Space Grotesk** for its utilitarian, monospaced-adjacent personality. It bridges the gap between 8-bit tech and high-end editorial design.



* **Display (L/M/S):** Used for mission headers and critical status codes. Always uppercase with -2% letter spacing to feel dense and authoritative.

* **Headline & Title:** Used for module labels. These should feel like labels on a physical machine.

* **Body:** Strictly `on-surface` (#E2E2E2) or `on-surface-variant` (#E7BCB8) for readability.

* **Label:** Used for metadata. Always paired with a `surface-container-highest` background to look like a "tag."



## 5. Elevation & Depth: The Rim Light

Traditional drop shadows are forbidden. To create depth in a dark environment, we use **Reverse Lighting**.



* **The Rim Light:** Instead of a shadow falling *away* from the object, use a 1px inner-shadow or top-border of `secondary-fixed` (#E2E2E2) at 20% opacity to simulate light hitting the top edge of a physical button.

* **Solid Offsets:** For "floating" elements, use a solid, 100% opaque black shadow (`#000000`) offset by 4px or 8px. This mimics the "sprite" layering of 8-bit retro games.

* **Glassmorphism:** For overlays (Modals/Tooltips), use `surface-container-highest` at 80% opacity with a `20px` backdrop blur. This ensures the "Night Mission" grid remains visible beneath the UI.



## 6. Components



### Buttons (Tactile Triggers)

* **Primary:** `primary-container` background, `on-primary-container` text. No curves. 2px solid `on-surface` border.

* **Secondary:** Ghost style. Transparent background, 2px `on-surface` border. Text in `on-surface`.

* **State Change:** On hover/active, the button should "glow"—add a 0px 0px 15px spread shadow using the `primary` color.



### Cards & Modules

Cards must not use soft shadows. They are defined by a `surface-container-high` fill and a 2px `outline-variant` border. To separate content within a card, use a change in background color (`surface-container-highest`) rather than a horizontal line.



### Input Fields

Rectangular blocks. `surface-container-lowest` background to create an "inset" look. Focus state is indicated by the 2px `primary` border and a blinking square cursor.



### Chips & Tags

Small, sharp-edged rectangles. Use `secondary-container` with `label-md` typography. These should look like technical specs printed on hardware.



## 7. Do's and Don'ts



### Do

* **Use the Grid:** Align all elements to the 16px grid.

* **Embrace Asymmetry:** Place high-contrast primary accents off-center to drive visual interest.

* **Check Contrast:** Ensure all red-on-dark combinations meet WCAG AA standards for mission-critical info.

* **Maintain 0px Radius:** Everything must be sharp. If it's round, it's wrong.



### Don't

* **No Soft Shadows:** Avoid fuzzy, grey ambient occlusion. Stick to hard offsets or rim lights.

* **No 1px Dividers:** Use vertical space or tonal shifts (e.g., `surface-container-low` vs `surface-container-high`).

* **No Gradients:** Keep surfaces flat and matte. The "soul" comes from the neon accents and the grid texture, not color fades.

* **No Standard Iconography:** Icons should be thick-stroked and geometric, following the same "sharp corner" rules as the containers.



## 8. Theme Object (Implementation)
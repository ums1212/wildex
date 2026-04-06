# Design System Documentation

## 1. Overview & Creative North Star: "The Tactile Cartridge"

This design system is a sophisticated love letter to the 8-bit era. Moving beyond simple nostalgia, it adopts a **Creative North Star of "The Tactile Cartridge."** We are not just building a website; we are building a physical object trapped inside a screen. 

The aesthetic ignores modern "airy" minimalism in favor of **Intentional Density** and **Hyper-Physicality**. By utilizing thick, non-standard borders and chunky, beveled surfaces, we create a UI that feels like it has weight, friction, and a satisfying "click." We break the traditional grid through staggered, offset containers that mimic the hardware constraints of 1990s handheld consoles, delivering an editorial experience that feels curated rather than templated.

---

## 2. Colors: High-Voltage Contrast

Our palette is built on the aggressive tension between `primary` (#bc0100) and high-neutral surfaces. This is a high-contrast system designed for immediate visual impact.

### Surface Hierarchy & Nesting
Traditional flat layouts are forbidden. Depth is achieved through "Cartridge Layering":
- **Base Level:** `surface` (#f9f9f9) acts as the outer casing.
- **Sunken Well:** Use `surface_container_low` (#f3f3f4) to create "inset" areas where content resides.
- **Raised Module:** Place `surface_container_lowest` (#ffffff) elements inside low containers to create a "pop-up" effect.

### The "No-Line" Exception & The Thick Border Rule
In this system, we reject the "No-Line" rule common in modern SaaS. Instead, we embrace the **4px Pixel-Stroke**. 
- **The Rule:** Boundaries are defined by a solid 4px border using `on_surface` (#1a1c1c). This border must never be 1px.
- **Shadow as Mass:** Avoid soft blurs. Instead, use "Hard Offsets." To create depth, shift a `surface_container_highest` block 4px down and 4px right, placing a solid `secondary` (#5d5f5f) block behind it to mimic a 3D plastic mold.

### Signature Textures
To add "soul," use a subtle 2x2 pixel grid overlay (using a repeating SVG pattern at 5% opacity) across `primary_container` sections. This simulates the screen door effect of vintage LCD panels.

---

## 3. Typography: The Digital Monolith

The typography pairing reflects the transition from hardware to software.

- **Display & Headline (Space Grotesk):** Our "Modern Pixel" font. Its technical, slightly quirky geometry mimics the precision of early digital typesetting. Use `display-lg` for hero moments to command the page like a game title screen.
- **Body & Title (Public Sans):** This acts as our "Player Manual" text. It is clean, neutral, and provides the necessary legibility to balance the aggressive display type.

**The "Text Box" Protocol:** All critical UI text must be contained within a high-contrast box. Never let text float over complex backgrounds; it must always sit on a `surface` or `surface_container_lowest` background to ensure the 8-bit "Dialogue Box" aesthetic is maintained.

---

## 4. Elevation & Depth: Tonal & Physical Layering

We do not use light-source shadows. We use **Tonal Stacking** and **Geometric Offsets**.

- **The Layering Principle:** Treat the UI as a series of stacked plastic plates. An "Active" element (like a card) should use `surface_container_highest` (#e2e2e2) with a 4px `on_surface` border.
- **The "Ghost Border" Fallback:** If a subtle separation is needed without the "Cartridge" weight, use `outline_variant` (#ebbbb4) but maintain the 4px width. Never thin the line; change the color instead.
- **Glassmorphism:** Reserved exclusively for "Pause Menus" or "Overlays." Use `surface` at 85% opacity with a `backdrop-filter: blur(0px)`. Do not blur the pixels; we want a "transparent plastic" look (like a Clear Edition Gameboy), not a frosted glass look.

---

## 5. Components: The Tactile Kit

### Buttons (The "Chunky Click")
- **Primary:** `primary` (#bc0100) background, `on_primary` (#ffffff) text.
- **The Bevel:** A 4px solid border of `on_surface`. To create the "pressed" state, remove the bottom/right offset shadow and shift the entire button 4px down.
- **Shape:** Strictly `0px` border-radius. Every corner is a sharp right angle.

### Cards (The "Dialogue Box")
- Cards must use a 4px border. 
- Headers should be separated from the body by a 4px horizontal line, mimicking the classic Pokémon stat screens.
- Use `surface_container_lowest` for the card body to make it "pop" from the `surface` background.

### Input Fields
- **Default:** `surface_container_low` with a 4px `on_surface` border.
- **Focus:** The border color shifts to `primary`, and the background shifts to `surface_container_lowest`.
- **Placeholder:** Use `secondary` (#5d5f5f) to maintain a "dimmed" electronic look.

### Selection Chips
- Square-edged. When unselected, they use `surface_variant`. When selected, they invert: `on_surface` background with `surface` text. This creates a high-contrast "Active" state.

---

## 6. Do's and Don'ts

### Do:
- **Use Intentional Asymmetry:** Offset your containers. If a container has a shadow, make it a hard-edged block shadow offset by 4px or 8px.
- **Embrace Red:** Use `primary` (#bc0100) for all interactive "Call to Action" moments. It should feel like the "A" button on a controller.
- **Strict Squareness:** Adhere to the `0px` roundedness scale. Any curve breaks the 8-bit immersion.

### Don't:
- **No Soft Shadows:** Never use `box-shadow` with a blur radius greater than 0. If it’s not sharp, it’s not in the system.
- **No 1px Lines:** 1px lines are too "fragile" for this system. If a divider is needed, use a 4px gap of background color or a 4px solid line.
- **No Centered Layouts:** Avoid perfectly centered, floating "Modern Web" layouts. Align elements to a rigid, blocky grid that feels like it was programmed in Assembly.

---

## 7. The Dark Variant: "Midnight Version"

When switching to the Dark Theme, the `on_surface` (#1a1c1c) becomes the primary background, and `surface` becomes a deep charcoal. The `primary` red remains constant, but its glow is amplified.
- **Inverted Borders:** Borders switch from `on_surface` to `surface_container_highest` to maintain the "etched" look in the dark.
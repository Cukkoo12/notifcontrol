# NotifControl

> **Full control over Minecraft toast notifications** — filter, reposition, scale, theme, and more.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![MC Version](https://img.shields.io/badge/Minecraft-26.1.2-brightgreen)]()
[![Fabric](https://img.shields.io/badge/Fabric-0.18.5-blue)]()
[![NeoForge](https://img.shields.io/badge/NeoForge-26.1.0.1--beta-orange)]()
[![Forge](https://img.shields.io/badge/Forge-26.1.2--64.0.8-red)]()

---

## 📋 Table of Contents

- [Features](#-features)
- [Installation](#-installation)
- [Commands](#-commands)
- [Configuration](#-configuration)
- [Keybindings](#-keybindings)
- [Themes](#-themes)
- [Profiles](#-profiles)
- [Building](#-building)
- [Project Structure](#-project-structure)

---

## ✨ Features

### Toast Filtering
- **Block or allow** toasts by type: Recipe Unlock, Advancement, Tutorial Hint, System
- **Blacklist/Whitelist** specific advancements by name
- **Regex support** for advanced filtering patterns
- Block specific system toast messages

### Appearance & Layout
- **Custom position** — TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT, or freeform anchor
- **Scale & opacity** — per-type or global
- **Custom offset** (X/Y) for fine-tuned positioning
- **Corner radius** for rounded toast backgrounds
- **Custom background color** per toast type (ARGB)
- **Hide icons** per toast type

### Animation
- **Animation styles** — SLIDE, FADE, BOUNCE, INSTANT
- **Adjustable animation speed**
- **Toast physics** — subtle bounce/spring on appear
- **Progress bar** showing remaining display time

### Smart Behavior
- **Do Not Disturb mode** — silently queue all toasts
- **Smart merging** — collapse duplicate toasts
- **Compact mode** — smaller, less intrusive toasts
- **Max visible toasts** limit
- **Spam delay** — minimum ms between same-type toasts
- **Duration multiplier** — global or per-type

### Themes
- `VANILLA` — default Minecraft look
- `HOLOGRAPHIC` — translucent blue-tinted glassmorphism
- `RGB` — animated rainbow cycling background

### Sound
- **Mute sound** per toast type
- **Custom sound event** — play any MC sound on toast appear

### Chat → Toast
- Convert chat messages to toasts based on **trigger words**
- Supports `@` mentions, whispers, and custom keywords
- Option to block the original chat message

### Notification History
- In-game **Notification Center** screen (press `H` by default)
- Browse, re-read, and dismiss past notifications
- Persists across sessions

### Profiles
- Save/load your full config as named profiles
- Switch between setups instantly via command
- Export/Import config as JSON

---

## 📦 Installation

### Fabric
1. Install [Fabric Loader 0.18.5+](https://fabricmc.net/use/) for Minecraft **26.1.2**
2. Install [Fabric API 0.145.4+](https://modrinth.com/mod/fabric-api)
3. Drop `notifcontrol-1.0.0.jar` into your `.minecraft/mods/` folder

### NeoForge
1. Install [NeoForge 26.1.0.1-beta](https://neoforged.net/) for Minecraft **26.1**
2. Drop `notifcontrol-neoforge-1.0.0.jar` into your `.minecraft/mods/` folder

### Forge
1. Install [Forge 26.1.2-64.0.8](https://files.minecraftforge.net/) for Minecraft **26.1.2**
2. Drop `notifcontrol-forge-1.0.0.jar` into your `.minecraft/mods/` folder

> **Note:** This is a **client-side only** mod. It does not need to be installed on servers.

---

## 💬 Commands

All commands start with `/toast` and require **no permissions** (usable by all players in singleplayer; op level 2 on servers).

```
/toast toggle                         — Toggle all toast types on/off
/toast status                         — Show current enabled/disabled state of all types
/toast type <recipe|advancement|tutorial|system> <on|off>
                                      — Enable or disable a specific toast type
/toast scale <0.1–5.0>                — Set global toast scale
/toast opacity <0.0–1.0>              — Set global toast opacity
/toast position <TOP_RIGHT|TOP_LEFT|BOTTOM_RIGHT|BOTTOM_LEFT>
                                      — Set toast anchor position
/toast duration <multiplier>          — Set duration multiplier (e.g. 2.0 = twice as long)
/toast theme <VANILLA|HOLOGRAPHIC|RGB>
                                      — Switch visual theme
/toast dnd <on|off>                   — Toggle Do Not Disturb mode
/toast send <type> <title> [message]  — Manually trigger a test toast
/toast history                        — Open the Notification Center
/toast profile save <name>            — Save current config as a named profile
/toast profile load <name>            — Load a previously saved profile
/toast profile list                   — List all saved profiles
/toast profile delete <name>          — Delete a saved profile
/toast export                         — Print config JSON to chat (for backup)
/toast reload                         — Reload config from disk
```

---

## ⚙️ Configuration

Config file is stored at:
- **Fabric/NeoForge/Forge:** `.minecraft/config/notifcontrol.json`

```json
{
  "recipe":      { "enabled": true, "durationMultiplier": 1.0, "scale": 1.0, "bgColor": -5218256, "hideIcon": false, "muteSound": false },
  "advancement": { "enabled": true, "durationMultiplier": 1.0, "scale": 1.0, "bgColor": -11890471 },
  "tutorial":    { "enabled": true, "durationMultiplier": 1.0, "scale": 1.0, "bgColor": -11483016 },
  "system":      { "enabled": true, "durationMultiplier": 1.0, "scale": 1.0, "bgColor": -8355712  },

  "toastScale": 1.0,
  "toastOpacity": 1.0,
  "toastPosition": "TOP_RIGHT",
  "toastOffsetX": 0,
  "toastOffsetY": 0,
  "animationStyle": "SLIDE",
  "animationSpeed": 1.0,
  "maxVisibleToasts": 5,
  "toastSpamDelayMs": 0,
  "toastCornerRadius": 8,

  "dndMode": false,
  "smartMerging": true,
  "compactMode": false,
  "toastPhysics": true,
  "progressBar": true,
  "themeMode": "VANILLA",

  "customSoundEnabled": false,
  "customSoundEvent": "minecraft:entity.experience_orb.pickup",

  "systemToastFilter": [],
  "advancementBlacklist": [],
  "filterMode": "BLACKLIST",
  "regexEnabled": false,

  "chatToToastEnabled": false,
  "chatTriggerWords": ["@", "whispers to you"],
  "blockOriginalChat": false,

  "globalToggleKey": -1
}
```

### Color Format
Colors use the **ARGB integer** format. You can convert using: `(alpha << 24) | (red << 16) | (green << 8) | blue`. Alpha `0xFF` = fully opaque.

---

## ⌨️ Keybindings

| Key | Default | Action |
|-----|---------|--------|
| Toggle Toasts | `Unbound` | Turn all toasts on/off |
| Notification History | `H` | Open the Notification Center |

Rebind via **Options → Controls → NotifControl** in-game.

---

## 🎨 Themes

| Theme | Description |
|-------|-------------|
| `VANILLA` | Default Minecraft toast appearance |
| `HOLOGRAPHIC` | Blue-tinted glassmorphism with translucent background |
| `RGB` | Animated rainbow background that cycles through colors |

Switch with `/toast theme <name>` or via the config screen.

---

## 💾 Profiles

Profiles let you save and restore complete configurations:

```
/toast profile save gaming      # Save current config as "gaming"
/toast profile save streaming   # Save another config as "streaming"
/toast profile load gaming      # Switch back to gaming config
/toast profile list             # Show all saved profiles
```

Profile files are stored at: `.minecraft/config/notifcontrol/profiles/`

---

## 🔨 Building

Requires **Java 25** (MC 26.1.x). Each loader is an independent Gradle project.

```powershell
# Fabric
cd fabric && .\gradlew.bat build

# NeoForge
cd neoforge && .\gradlew.bat build

# Forge
cd forge && .\gradlew.bat build
```

Output JARs are in each loader's `build/libs/` directory.

### Dev Environment

```powershell
# Run client for testing (Fabric & NeoForge work; Forge dev run is WIP)
cd fabric   && .\gradlew.bat runClient
cd neoforge && .\gradlew.bat runClient
```

---

## 🗂️ Project Structure

```
NotifControl/
├── README.md
├── .gitignore
│
├── fabric/                  — Fabric Loader port (MC 26.1.2, Fabric 0.18.5)
│   ├── build.gradle
│   ├── gradle.properties
│   └── src/main/java/com/cukkoo/notifcontrol/
│       ├── NotifControlMod.java          — Fabric entrypoint
│       ├── NotifControlConfig.java       — JSON config system
│       ├── NotifControlCommand.java      — /toast command tree
│       ├── NotifControlHistory.java      — Toast history storage
│       ├── ChatToast.java                — Chat → Toast converter
│       ├── mixin/
│       │   ├── ToastManagerMixin.java    — Intercepts toast queue
│       │   ├── ToastInstanceMixin.java   — Overrides render/display
│       │   ├── NotifControlMixin.java    — GUI render state hook
│       │   ├── ChatComponentMixin.java   — Chat interception
│       │   └── AdvancementToastAccessor — Advancement toast data
│       └── modmenu/
│           ├── NotifControlScreen.java        — Main settings screen
│           ├── NotifControlPositionScreen.java — Visual position picker
│           ├── NotificationCenterScreen.java   — History viewer
│           └── NotifControlModMenuEntry.java   — ModMenu integration
│
├── neoforge/                — NeoForge port (MC 26.1, NeoForge 26.1.0.1-beta)
│   └── ...                  — Same structure, NeoForge event bus
│
└── forge/                   — Forge port (MC 26.1.2, Forge 26.1.2-64.0.8)
    └── ...                  — Same structure, Forge event bus
```

---

## 📄 License

MIT — © 2026 [Cukkoo12](https://github.com/Cukkoo12)

---

## 🤝 Contributing

Issues and PRs welcome at [github.com/Cukkoo12/notifcontrol](https://github.com/Cukkoo12/notifcontrol).

When contributing:
- Keep platform-specific code in the respective loader folder
- Test on all three loaders before submitting
- Follow existing code style (no Lombok, pure Java)

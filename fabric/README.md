# NotifControl (V3 Ultimate)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.4-blue.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Loader-orange.svg)](https://fabricmc.net/)

**NotifControl** is an enterprise-grade Fabric mod for Minecraft 1.21.4 that grants you absolute, granular control over Minecraft's toast notifications. Whether you want to mute recipe spam, reposition & scale alerts, view notification history, or completely theme your toasts with dynamic RGB gradients, NotifControl provides a fully localized, high-performance solution.

---

## ✨ Features

### 🛡️ Filter & Blacklist System
* **Fine-Grained Filtering:** Separately toggle **Recipes, Advancements, Tutorials, and System/Server** notifications.
* **Granular Blacklists:** Put specific advancements, recipes, or system keywords on a blacklist.
* **Regex Filtering:** Advanced users can filter system notifications using regular expressions.

### 🎛️ Repositioning, Scaling & Transparency
* **Dynamic Placement:** Change screen corners or set freeform X/Y offsets.
* **Scalable UI:** Scale toasts from `0.5x` up to `2.0x` globally or per-type to fit custom GUIs.
* **Opacity Settings:** Adjust background opacity for cleaner HUD integration.

### 🎨 Ultimate Theming & Custom Styling
* **Custom Background Colors:** Choose a specific hex color (ARGB) for recipe, advancement, system, or tutorial toasts.
* **Text Customization:** Override Title and Description text colors independently.
* **Theme Modes:** Choose between **Vanilla**, **Dynamic RGB**, and **Holographic** modern styles.
* **Adjustable Corner Radius:** Square off or round the edges of your notifications.

### ⚡ Smart Merging & Animation physics
* **Compact Mode & Smart Merging:** Prevent screen clutter by auto-grouping duplicate/spammy notifications.
* **Toast Physics:** Dynamic gravity-based animations when notifications appear.
* **Animation Styles:** Toggle fluid animation presets (e.g., Slide-in).
* **Progress Bars:** Optional visual timer indicator on each toast.

### 📣 Chat-to-Toast Integration
* Instantly convert chat highlights, private whispers, or messages containing `@` into toast notifications!
* Optional setting to block original chat messages once they are converted to toasts to prevent duplicate HUD pollution.

### 🎵 Custom Notification Sounds
* Play a custom sound effect when a toast triggers.
* Configure any sound resource ID (e.g., `minecraft:entity.experience_orb.pickup`) and toggle sound triggers per type.

### 📋 Persistent Notification History Center
* Missed a notification during combat?
* Press **`H`** to open a beautiful **Notification History Center** to review, search, and manage a complete log of all toasts received during your session.

---

## ⌨️ Controls & Hotkeys

* **`H`** (Default): Open the Notification History Center.
* **Global Toggle Key** (Unbound by default): Instantly toggle Do Not Disturb (DND) mode on/off.
* Fully customizable in Minecraft's default **Controls** menu.

---

## 💬 Brigadier Commands

NotifControl integrates seamlessly with Brigadier autocomplete:

### ⚙️ Main Commands
* `/toast config` — Open the graphical configuration menu.
* `/toast history` — Open the Notification History Center.
* `/toast send <recipe|advancement|tutorial|system>` — Trigger a preview toast for debugging.
* `/toast toggle <recipe|advancement|tutorial|system|all>` — Instantly toggle notification filters.

### 📁 Profile Management
Create, swap, and delete multiple layout profiles:
* `/toast profile save <name>` — Save your current layout & configuration under a name.
* `/toast profile load <name>` — Load a previously saved profile.
* `/toast profile list` — Display all saved profiles in chat.
* `/toast profile delete <name>` — Permanently delete a saved profile.

---

## 📄 Configuration Structure (`notifcontrol.json`)

The config file is located at `.minecraft/config/notifcontrol.json`. Advanced users can edit it manually:

```json
{
  "recipe": {
    "enabled": true,
    "durationMultiplier": 1.0,
    "scale": 1.0,
    "bgColor": -5218256,
    "hideIcon": false,
    "titleColor": -1,
    "descColor": -5592406,
    "muteSound": false
  },
  "advancement": {
    "enabled": true,
    "durationMultiplier": 1.0,
    "scale": 1.0,
    "bgColor": -11890471,
    "hideIcon": false,
    "titleColor": -1,
    "descColor": -5592406,
    "muteSound": false
  },
  "toastDurationMultiplier": 1.0,
  "maxVisibleToasts": 5,
  "toastSpamDelayMs": 0,
  "toastScale": 1.0,
  "toastOpacity": 1.0,
  "toastPosition": "TOP_RIGHT",
  "toastOffsetX": 0,
  "toastOffsetY": 0,
  "animationStyle": "SLIDE",
  "animationSpeed": 1.0,
  "stackingEnabled": false,
  "customBackgroundsEnabled": false,
  "toastCornerRadius": 8,
  "dndMode": false,
  "smartMerging": true,
  "compactMode": false,
  "toastPhysics": true,
  "progressBar": true,
  "themeMode": "VANILLA",
  "customSoundEnabled": false,
  "customSoundEvent": "minecraft:entity.experience_orb.pickup",
  "freeformAnchor": false,
  "systemToastFilter": [],
  "advancementBlacklist": [],
  "filterMode": "BLACKLIST",
  "regexEnabled": false,
  "logBlockedToasts": false,
  "chatToToastEnabled": false,
  "chatTriggerWords": [
    "@",
    "whispers to you"
  ],
  "blockOriginalChat": false,
  "globalToggleKey": -1
}
```

---

## 🛠️ Installation & Requirements

* **Fabric Loader** (>= 0.18.5)
* **Fabric API** (>= 0.145.4)
* **Minecraft** (1.21.4 / 26.1.2)
* **Mod Menu** (Highly recommended, to easily click the "Settings" button)

Put the `.jar` file inside your `.minecraft/mods/` folder and launch the game.

---

## 🏗️ Building from Source

To compile and package the mod:

```bash
# Clone the repository
git clone https://github.com/Cukkoo12/notifcontrol.git
cd notifcontrol

# Compile and package jar
./gradlew build
```
Your compiled file will be located in `build/libs/notifcontrol-<version>.jar`.

---

## 📄 License

Distributed under the **MIT License**. See the `LICENSE` file for details.

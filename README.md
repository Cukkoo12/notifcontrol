# NotifControl

Full control over Minecraft toast notifications — filter, reposition, scale, theme and more.

## Loaders

| Loader | MC Version | Folder |
|--------|-----------|--------|
| Fabric | 26.1.2 | `fabric/` |
| NeoForge | 26.1.x | `neoforge/` |
| Forge | 26.1.2 | `forge/` |

## Building

```powershell
# Fabric
cd fabric && .\gradlew.bat build

# NeoForge
cd neoforge && .\gradlew.bat build

# Forge
cd forge && .\gradlew.bat build
```

## Features
- Block/allow toasts by type (Recipe, Advancement, Tutorial, System)
- Custom position, scale, opacity, animation style
- Themes: Default, Holographic, RGB
- Notification history center
- Profile save/load
- In-game commands: `/toast toggle|status|send|profile|history`

## License
MIT — © Cukkoo12

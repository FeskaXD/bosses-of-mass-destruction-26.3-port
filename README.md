# Bosses of Mass Destruction — 26.3 Port

An **unofficial port** of [Bosses of Mass Destruction](https://modrinth.com/mod/bosses-of-mass-destruction) by **Barribob** to **Minecraft 26.3** on **Fabric**.

The original mod stopped at Minecraft 1.21.1. This port brings its four boss fights to the current version, with the original gameplay, models and animations kept as close as possible.

> All credit for the mod's design, code, models, textures and sounds goes to the original author.
> Original source: https://github.com/miyo6032/bosses-of-mass-destruction

## Bosses

- **Night Lich**: summons minions and rains magic missiles.
- **Obsidilith**: a pillar of obsidian with waves, spikes and a shield phase.
- **Gauntlet**: a giant floating hand with laser and punch attacks.
- **Void Blossom**: a plant boss that spreads thorns and spores.

Each boss has its own structure, loot and boss bar.

## Requirements

| Mod | Version |
|---|---|
| [Fabric Loader](https://fabricmc.net/use/) | 0.19.0+ |
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.161.0+26.3 |
| [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) | 1.14.0+ |
| [GeckoLib](https://modrinth.com/mod/geckolib) | 5.5+ |
| [Cardinal Components API](https://modrinth.com/mod/cardinal-components-api) | 8.0.0+ |
| [Cloth Config](https://modrinth.com/mod/cloth-config) | 26.0.0+ |

Needed on both the client and the server.

## What changed in the port

- Moved from Yarn to official Mojang mappings.
- Updated to GeckoLib 5 and the 26.x render-state rendering system.
- Updated saving, items, blocks, tooltips, advancements, loot tables and recipes to the 26.x formats.
- [MultipartEntities](https://github.com/miyo6032/MultipartEntities) by stuff_stuffs (MIT, fork used by the original mod) is bundled inside the jar, the same way the original did.


## License

This port is licensed under the **GNU LGPL-3.0**, the same as the original. See [LICENSE](LICENSE). The bundled MultipartEntities code is MIT, see [LICENSE-MultipartEntities](LICENSE-MultipartEntities).

This is not an official release. Please report problems with the port here, not to the original author.

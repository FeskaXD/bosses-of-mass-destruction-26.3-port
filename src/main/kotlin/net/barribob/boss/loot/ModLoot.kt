package net.barribob.boss.loot

import net.barribob.boss.Mod
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries

class ModLoot {
    val gauntlet = ResourceKey.create(Registries.LOOT_TABLE, Mod.identifier("chests/gauntlet"))
    val obsidilith = ResourceKey.create(Registries.LOOT_TABLE, Mod.identifier("chests/obsidilith"))
}
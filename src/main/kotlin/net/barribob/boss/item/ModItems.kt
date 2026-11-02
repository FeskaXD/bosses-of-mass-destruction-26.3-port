package net.barribob.boss.item

import net.barribob.boss.Mod
import net.barribob.boss.block.BrimstoneNectarItem
import net.barribob.boss.block.structure_repair.GauntletStructureRepair
import net.barribob.boss.block.structure_repair.LichStructureRepair
import net.barribob.boss.block.structure_repair.ObsidilithStructureRepair
import net.barribob.boss.block.structure_repair.VoidBlossomStructureRepair
import net.barribob.boss.utils.ModUtils
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.component.Consumables
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Item
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Rarity

class ModItems {
    var itemGroup: ResourceKey<CreativeModeTab> = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Mod.identifier("items"))

    // 26.3: items must know their registry id when constructed
    private fun props(name: String): Item.Properties = Item.Properties().setId(ResourceKey.create(Registries.ITEM, Mod.identifier(name)))

    val soulStar = SoulStarItem(props("soul_star"))
    private val ancientAnima = MaterialItem(props("ancient_anima").rarity(Rarity.RARE))
    private val blazingEye = MaterialItem(props("blazing_eye").rarity(Rarity.RARE).fireResistant())
    private val obsidianHeart = MaterialItem(props("obsidian_heart").rarity(Rarity.RARE).fireResistant())
    private val earthdiveSpear = EarthdiveSpear(props("earthdive_spear").fireResistant().durability(250).enchantable(1)) // createAttributeModifiers()
    private val voidThorn = MaterialItem(props("void_thorn").rarity(Rarity.RARE).fireResistant())
    private val crystalFruitFoodComponent = FoodProperties.Builder().nutrition(4).saturationModifier(1.2f)
        .alwaysEdible().build()
    private val crystalFruitConsumable = Consumables.defaultFood()
        .onConsume(ApplyStatusEffectsConsumeEffect(listOf(
            MobEffectInstance(MobEffects.REGENERATION, 300, 1),
            MobEffectInstance(MobEffects.INSTANT_HEALTH, 1),
            MobEffectInstance(MobEffects.RESISTANCE, 600, 0))))
        .build()
    private val crystalFruit = CrystalFruitItem(props("crystal_fruit").rarity(Rarity.RARE).fireResistant().food(crystalFruitFoodComponent, crystalFruitConsumable))
    val chargedEnderPearl = ChargedEnderPearlItem(props("charged_ender_pearl").fireResistant().stacksTo(1))
    private val brimstoneNectar = BrimstoneNectarItem(
        props("brimstone_nectar").rarity(Rarity.RARE).fireResistant(), listOf(
        VoidBlossomStructureRepair(), GauntletStructureRepair(), ObsidilithStructureRepair(), LichStructureRepair()
    ))

    fun init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, itemGroup, FabricCreativeModeTab.builder()
            .icon { ItemStack(soulStar) }
            .title(Component.translatable("itemGroup.bosses_of_mass_destruction.items"))
            .build())
        
        registerItem(Mod.identifier("soul_star"), soulStar)
        registerItem(Mod.identifier("ancient_anima"), ancientAnima)
        registerItem(Mod.identifier("blazing_eye"), blazingEye)
        registerItem(Mod.identifier("obsidian_heart"), obsidianHeart)
        registerItem(Mod.identifier("earthdive_spear"), earthdiveSpear)
        registerItem(Mod.identifier("void_thorn"), voidThorn)
        registerItem(Mod.identifier("crystal_fruit"), crystalFruit)
        registerItem(Mod.identifier("charged_ender_pearl"), chargedEnderPearl)
        registerItem(Mod.identifier("brimstone_nectar"), brimstoneNectar)
    }
    
    private fun registerItem(identifier: Identifier, item: Item, addToItemGroup: Boolean = true){
        Registry.register(BuiltInRegistries.ITEM, identifier, item)
        if (addToItemGroup)
            ModUtils.addItemToGroup(item)
    }

    @Environment(EnvType.CLIENT)
    fun clientInit() {
        // 26.3: the spear's "throwing" model is selected in assets/.../items/earthdive_spear.json (minecraft:using_item)
    }
}

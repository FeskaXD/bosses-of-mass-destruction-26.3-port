package net.barribob.boss.block

import net.barribob.boss.Mod
import net.barribob.boss.mob.GeoModel
import net.barribob.boss.render.IBoneLight
import net.barribob.boss.render.ModBlockEntityRenderer
import net.barribob.boss.utils.ModUtils
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.block.state.BlockBehaviour
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType

import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.resources.Identifier

object ModBlocks {
    // 26.3: blocks must know their registry id when constructed
    private fun key(name: String): ResourceKey<Block> = ResourceKey.create(Registries.BLOCK, Mod.identifier(name))
    private fun props(name: String): BlockBehaviour.Properties = BlockBehaviour.Properties.of().setId(key(name))
    private fun copy(block: Block, name: String): BlockBehaviour.Properties = BlockBehaviour.Properties.ofFullCopy(block).setId(key(name))

    val obsidilithRune = ObsidilithRuneBlock(props("obsidilith_rune").mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(50.0F, 1200.0F))
    val voidBlossom = VoidBlossomBlock(props("void_blossom").mapColor(MapColor.COLOR_BLACK).instabreak().noCollision().lightLevel { 11 }.sound(SoundType.SPORE_BLOSSOM))
    val vineWall = VineWallBlock(props("vine_wall").mapColor(MapColor.COLOR_GREEN).sound(SoundType.WOOD).strength(2.0f, 6.0f))
    val obsidilithSummonBlock = ObsidilithSummonBlock(copy(Blocks.END_PORTAL_FRAME, "obsidilith_end_frame"))
    val gauntletBlackstone = GauntletBlackstoneBlock(props("gauntlet_blackstone").mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(50.0F, 1200.0F))
    val sealedBlackstone = Block(copy(Blocks.BEDROCK, "sealed_blackstone"))
    val chiseledStoneAltar = ChiseledStoneAltarBlock(
        copy(Blocks.BEDROCK, "chiseled_stone_altar")
            .lightLevel { if (it.getValue(BlockStateProperties.LIT)) 11 else 0 })

    var mobWardEntityType: BlockEntityType<ChunkCacheBlockEntity>? = null
    private val mobWardBlockEntityFactory = FabricBlockEntityTypeBuilder.Factory { pos, state ->
        ChunkCacheBlockEntity(mobWard, mobWardEntityType!!, pos, state)
    }
    val mobWard: MobWardBlock = MobWardBlock(mobWardBlockEntityFactory, props("mob_ward").mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops()
        .noOcclusion()
        .lightLevel { 15 }
        .strength(10.0F, 1200.0F))

    var monolithEntityType: BlockEntityType<ChunkCacheBlockEntity>? = null
    private val monolithBlockEntityFactory = FabricBlockEntityTypeBuilder.Factory { pos, state ->
        ChunkCacheBlockEntity(monolithBlock, monolithEntityType!!, pos, state)
    }
    val monolithBlock: MonolithBlock = MonolithBlock(monolithBlockEntityFactory, props("monolith_block").mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops()
        .noOcclusion()
        .lightLevel { 4 }
        .strength(10.0F, 1200.0F))

    var levitationBlockEntityType: BlockEntityType<LevitationBlockEntity>? = null
    private val levitationBlockEntityFactory = FabricBlockEntityTypeBuilder.Factory { pos, state ->
        LevitationBlockEntity(levitationBlock, levitationBlockEntityType!!, pos, state)
    }
    val levitationBlock: LevitationBlock = LevitationBlock(levitationBlockEntityFactory, props("levitation_block").mapColor(MapColor.COLOR_BLUE).requiresCorrectToolForDrops()
        .noOcclusion()
        .lightLevel { 4 }
        .strength(10.0F, 1200.0F))

    var voidBlossomSummonBlockEntityType: BlockEntityType<VoidBlossomSummonBlockEntity>? = null
    private val voidBlossomSummonBlockEntityFactory = FabricBlockEntityTypeBuilder.Factory { pos, state ->
        VoidBlossomSummonBlockEntity(voidBlossomSummonBlockEntityType!!, pos, state)
    }
    val voidBlossomSummonBlock = VoidBlossomSummonBlock(voidBlossomSummonBlockEntityFactory, copy(Blocks.BEDROCK, "void_blossom_block"))

    var voidLilyBlockEntityType: BlockEntityType<VoidLilyBlockEntity>? = null
    private val voidLilyBlockEntityFactory = FabricBlockEntityTypeBuilder.Factory { pos, state ->
        VoidLilyBlockEntity(voidLilyBlockEntityType!!, pos, state)
    }
    private val voidLilyBlock = VoidLilyBlock(voidLilyBlockEntityFactory,
        props("void_lily").noCollision().randomTicks().instabreak()
            .sound(SoundType.GRASS).lightLevel { 8 })

    fun init() {
        registerBlockAndItem(Mod.identifier("obsidilith_rune"), obsidilithRune)
        registerBlockAndItem(Mod.identifier("obsidilith_end_frame"), obsidilithSummonBlock)
        registerBlockAndItem(Mod.identifier("gauntlet_blackstone"), gauntletBlackstone)
        registerBlockAndItem(Mod.identifier("sealed_blackstone"), sealedBlackstone)
        registerBlockAndItem(Mod.identifier("chiseled_stone_altar"), chiseledStoneAltar)
        registerBlockAndItem(Mod.identifier("void_blossom"), voidBlossom)
        registerBlockAndItem(Mod.identifier("vine_wall"), vineWall)

        val mobWardId = Mod.identifier("mob_ward")
        val monolithBlockId = Mod.identifier("monolith_block")
        val levitationBlockId = Mod.identifier("levitation_block")
        val voidBlossomBlockId = Mod.identifier("void_blossom_block")
        val voidLilyBlockId = Mod.identifier("void_lily")

        registerBlockAndItem(mobWardId, mobWard, true)
        registerBlockAndItem(monolithBlockId, monolithBlock, true)
        registerBlockAndItem(levitationBlockId, levitationBlock, true)
        registerBlockAndItem(voidBlossomBlockId, voidBlossomSummonBlock, false)
        registerBlockAndItem(voidLilyBlockId, voidLilyBlock, true)
 
        mobWardEntityType = registerBlockEntityType(mobWardId, mobWardBlockEntityFactory, mobWard)
        monolithEntityType = registerBlockEntityType(monolithBlockId, monolithBlockEntityFactory, monolithBlock)
        levitationBlockEntityType = registerBlockEntityType(levitationBlockId, levitationBlockEntityFactory, levitationBlock)
        voidBlossomSummonBlockEntityType = registerBlockEntityType(voidBlossomBlockId, voidBlossomSummonBlockEntityFactory, voidBlossomSummonBlock)
        voidLilyBlockEntityType = registerBlockEntityType(voidLilyBlockId, voidLilyBlockEntityFactory, voidLilyBlock)
    }

    fun clientInit() {
        // 26.3: cutout render layers are picked automatically from texture transparency
        BlockEntityRendererRegistry.register(levitationBlockEntityType!!) { context ->
            ModBlockEntityRenderer(
                context,
                GeoModel<LevitationBlockEntity>(
                    { Mod.identifier("levitation_block") },
                    { Mod.identifier("textures/block/levitation_block.png") },
                    Mod.identifier("levitation_block"),
                )
            ) { _, _ -> IBoneLight.fullbright }
        }
    }
    
    private fun <T : BlockEntity> registerBlockEntityType(identifier: Identifier, factory: FabricBlockEntityTypeBuilder.Factory<T>, block: Block): BlockEntityType<T>{
        return Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            identifier,
            FabricBlockEntityTypeBuilder.create(factory, block).build()
        )
    }

    private fun registerBlockAndItem(identifier: Identifier, block: Block, addToItemGroup: Boolean = false) {
        Registry.register(BuiltInRegistries.BLOCK, identifier, block)
        val blockItem = TooltipBlockItem(block, Item.Properties().setId(ResourceKey.create(Registries.ITEM, identifier)).useBlockDescriptionPrefix())
        Registry.register(BuiltInRegistries.ITEM, identifier, blockItem)
        if (addToItemGroup)
            ModUtils.addItemToGroup(blockItem)   
    }
}
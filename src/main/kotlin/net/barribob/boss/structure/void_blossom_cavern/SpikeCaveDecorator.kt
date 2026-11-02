package net.barribob.boss.structure.void_blossom_cavern

import com.mojang.datafixers.util.Pair
import net.barribob.boss.structure.util.IStructurePiece
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.chunk.ChunkGenerator
import kotlin.math.pow

class SpikeCaveDecorator(private val bottomOfWorld: Int, private val random: RandomSource) : ICaveDecorator {
    private val spikePositions = mutableListOf<BlockPos>()
    private val baseBlocks = MathUtils.buildBlockCircle(4.2)

    override fun onBlockPlaced(pos: BlockPos, block: Block) {
        val spikeSpacing = (random.nextInt(20) + 10).toDouble().pow(2)
        if (pos.y == 5 + bottomOfWorld && block != Blocks.AIR && spikePositions.all { it.distSqr(pos) > spikeSpacing }) {
            spikePositions.add(pos)
        }
    }

    override fun generate(
        world: WorldGenLevel,
        chunkGenerator: ChunkGenerator,
        random: RandomSource,
        boundingBox: BoundingBox,
        pos: BlockPos,
        structurePiece: IStructurePiece
    ) {
        val spacesSpikePositions = spikePositions.groupBy { Pair(it.x shr 2, it.z shr 2) }.map { it.value.first() }
        for (outerPos in spacesSpikePositions) {
            val centerDirection = pos.subtract(outerPos).asVec3d().planeProject(VecUtils.yAxis).normalize()
            val tip = centerDirection
                .scale((5 + random.nextInt(3)).toDouble())
                .add(VecUtils.yAxis.scale((7 + random.nextInt(5)).toDouble()))
            generateSpike(outerPos, outerPos.offset(BlockPos.containing(tip)), structurePiece, world, boundingBox, centerDirection)
        }
    }

    private fun generateSpike(
        origin: BlockPos,
        tip: BlockPos,
        structurePiece: IStructurePiece,
        world: WorldGenLevel,
        boundingBox: BoundingBox,
        centerDirection: Vec3
    ) {
        val centerDirectionPos = BlockPos.containing(centerDirection.scale(3.0))
        val blockSet = baseBlocks.map{BlockPos.containing(it)}.toSet()
        val innerBlockSet = blockSet.filter { it.distSqr(centerDirectionPos) < 2.0.pow(2) }.toSet()
        val middleBlockSet = blockSet.subtract(innerBlockSet).filter { it.distSqr(centerDirectionPos) < 3.7.pow(2) }.toSet()
        val outerBlockSet = blockSet.subtract(middleBlockSet).subtract(innerBlockSet)

        for (block in innerBlockSet) {
            val blocksInLine = MathUtils.getBlocksInLine(block.offset(origin), tip)
            for (pos in blocksInLine) {
                val blockState = if(random.nextInt(16) == 0) Blocks.BUDDING_AMETHYST.defaultBlockState() else Blocks.AMETHYST_BLOCK.defaultBlockState()
                structurePiece.addBlock(world, blockState, pos, boundingBox)
            }
        }
        for (block in middleBlockSet) {
            val blocksInLine = MathUtils.getBlocksInLine(block.offset(origin), tip)
            for (pos in blocksInLine) {
                structurePiece.addBlock(world, Blocks.CALCITE.defaultBlockState(), pos, boundingBox)
            }
        }
        for (block in outerBlockSet) {
            val blocksInLine = MathUtils.getBlocksInLine(block.offset(origin), tip)
            for (pos in blocksInLine) {
                structurePiece.addBlock(world, Blocks.SMOOTH_BASALT.defaultBlockState(), pos, boundingBox)
            }
        }
    }
}
package net.barribob.boss.structure.void_blossom_cavern

import com.google.common.collect.Lists
import com.mojang.datafixers.util.Pair
import net.barribob.boss.structure.util.IPieceGenerator
import net.barribob.boss.structure.util.IStructurePiece
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.util.Mth
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.levelgen.synth.NormalNoise
import net.minecraft.world.level.levelgen.LegacyRandomSource
import net.minecraft.world.level.levelgen.WorldgenRandom
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.feature.Feature
import kotlin.math.sqrt

class VoidBlossomCavernPieceGenerator : IPieceGenerator {

    override fun generate(
        world: WorldGenLevel,
        structureAccessor: StructureManager,
        chunkGenerator: ChunkGenerator,
        random: RandomSource,
        boundingBox: BoundingBox,
        chunkPos: ChunkPos,
        pos: BlockPos,
        structurePiece: IStructurePiece
    ): Boolean {
        val minY = chunkGenerator.minY
        val caveDecorators = listOf(
            SpikeCaveDecorator(minY, random),
            MossFloorCaveDecorator(minY, random),
            MossCeilingCaveDecorator(minY, random),
            SporeBlossomCaveDecorator(minY, random),
            BossBlockDecorator(minY)
        )

        generateCave(world, pos.above(17), structurePiece, random, boundingBox, caveDecorators, chunkGenerator.minY)

        for (decorator in caveDecorators) {
            decorator.generate(world, chunkGenerator, random, boundingBox, pos, structurePiece)
        }

        return true
    }

    private fun generateCave(
        world: WorldGenLevel,
        pos: BlockPos,
        structurePiece: IStructurePiece,
        random: RandomSource,
        boundingBox: BoundingBox,
        caveDecorators: List<ICaveDecorator>,
        bottomOfWorld: Int
    ) {
        val noiseMultiplier = 0.005
        val outerWallDistance = UniformInt.of(3, 4)
        val pointOffset = UniformInt.of(1, 2)
        val minY = bottomOfWorld - pos.y
        val maxY = minY + 32
        val minXZ = -32
        val maxXZ = 32
        val verticalSquish = 2.0
        val distributionPoints = 5

        val randoms: MutableList<Pair<BlockPos, Int>> = Lists.newLinkedList()
        val chunkRandom = WorldgenRandom(LegacyRandomSource(world.seed))
        val doublePerlinNoiseSampler = NormalNoise.createParity(-4, 1.0).create(chunkRandom)
        val d = distributionPoints.toDouble() / outerWallDistance.maxInclusive.toDouble()
        val airThickness = 1.0 / sqrt(25.2 + d)
        val outerLayerThickness = 1.0 / sqrt(30.2 + d)

        var randomPos: BlockPos
        var r = 0
        while (r < distributionPoints) {
            randomPos =
                pos.offset(outerWallDistance.sample(random), outerWallDistance.sample(random), outerWallDistance.sample(random))
            randoms.add(
                Pair.of(
                    randomPos,
                    pointOffset.sample(random)
                )
            )
            ++r
        }

        val predicate = java.util.function.Predicate<BlockState> { !it.`is`(BlockTags.FEATURES_CANNOT_REPLACE) }
        val positions: Iterator<BlockPos> =
            BlockPos.betweenClosed(pos.offset(minXZ, minY, minXZ), pos.offset(maxXZ, maxY, maxXZ)).iterator()
        while (true) {
            var noisedDistance: Double
            var samplePos: BlockPos
            do {
                if (!positions.hasNext()) {
                    return
                }

                samplePos = positions.next()

                val noise = doublePerlinNoiseSampler.get(
                    samplePos.x.toDouble(),
                    samplePos.y.toDouble(),
                    samplePos.z.toDouble()
                ).toDouble() * noiseMultiplier

                noisedDistance = 0.0
                val randomsIter: Iterator<Pair<BlockPos, Int>> = randoms.iterator()

                while (randomsIter.hasNext()) {
                    val pair = randomsIter.next()
                    val distancePos = BlockPos(samplePos.x, (((samplePos.y - bottomOfWorld) * verticalSquish) + bottomOfWorld).toInt(), samplePos.z)
                    noisedDistance += Mth.fastInvSqrt(distancePos.distSqr(pair.first) + (pair.second).toDouble()) + noise
                }
            } while (noisedDistance < outerLayerThickness)

            val canReplace = {
                predicate.test(world.getBlockState(samplePos)) &&
                        !world.getBlockState(samplePos).isAir &&
                        world.getBlockState(samplePos).block != Blocks.BEDROCK &&
                        samplePos.y > 4 + bottomOfWorld
            }

            if (noisedDistance >= airThickness) {
                replaceBlock(
                    canReplace,
                    world,
                    samplePos,
                    structurePiece,
                    boundingBox,
                    Blocks.AIR.defaultBlockState(),
                    caveDecorators
                )
            } else if (noisedDistance >= outerLayerThickness) {
                replaceBlock(
                    canReplace,
                    world,
                    samplePos,
                    structurePiece,
                    boundingBox,
                    if(samplePos.y > 0) Blocks.STONE.defaultBlockState() else Blocks.DEEPSLATE.defaultBlockState(),
                    caveDecorators
                )
            }
        }
    }

    private fun replaceBlock(
        predicate: () -> Boolean,
        world: WorldGenLevel,
        samplePos: BlockPos,
        structurePiece: IStructurePiece,
        boundingBox: BoundingBox,
        blockState: BlockState,
        caveDecorators: List<ICaveDecorator>
    ) {
        if (predicate()) structurePiece.addBlock(world, blockState, samplePos, boundingBox)

        for (decorator in caveDecorators) {
            decorator.onBlockPlaced(samplePos.immutable(), blockState.block)
        }
    }
}
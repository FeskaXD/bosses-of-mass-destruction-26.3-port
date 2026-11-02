package net.barribob.boss.render

import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.gui.components.BossHealthOverlay
import net.minecraft.world.BossEvent
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

class NodeBossBarRenderer(
    private val entityTypeKey: String,
    private val hpPercentages: List<Float>,
    private val noteTexture: Identifier,
    private val textureSize: Int
) {
    /**
     * Sourced from [BossHealthOverlay.renderBossBar]
     */
    fun renderBossBar(backgroundTexture: Array<Identifier>, progressTexture: Array<Identifier>, drawContext: GuiGraphicsExtractor, x: Int, y: Int, bossBar: BossEvent, callbackInfo: CallbackInfo) {
        val name = bossBar.name ?: return
        val barContent = name.contents
        if (barContent is TranslatableContents && barContent.key.equals(entityTypeKey)) {
            val colorLocation = bossBar.color.ordinal;
            drawContext.blitSprite(
                RenderPipelines.GUI_TEXTURED, backgroundTexture[colorLocation], 182, 5, 0, 0, x, y, 182, 5,
            )

            val i = Mth.lerpDiscrete(bossBar.progress, 0, 182);
            if (i > 0) {
                val progressLocation = bossBar.color.ordinal
                drawContext.blitSprite(
                    RenderPipelines.GUI_TEXTURED, progressTexture[progressLocation], 182, 5, 0, 0, x, y, i, 5
                )
            }

            renderBossNodes(bossBar, drawContext, x, y)

            callbackInfo.cancel()
        }
    }

    private fun renderBossNodes(
        bossBar: BossEvent,
        drawContext: GuiGraphicsExtractor,
        x: Int,
        y: Int,
    ) {
        val steppedPercentage = (192 * MathUtils.roundedStep(bossBar.progress, hpPercentages, true)).toInt() + 7
        drawContext.blit(
            RenderPipelines.GUI_TEXTURED, noteTexture, x - 3, y - 1, 0f, 0f, steppedPercentage, 7,
            textureSize,
            textureSize
        )

        val steppedPercentageReverse = 192 - steppedPercentage
        drawContext.blit(
            RenderPipelines.GUI_TEXTURED,
            noteTexture,
            x - 3 + steppedPercentage,
            y - 1,
            steppedPercentage.toFloat(),
            7f,
            steppedPercentageReverse,
            7,
            textureSize,
            textureSize
        )
    }
}
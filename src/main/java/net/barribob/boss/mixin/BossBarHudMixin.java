package net.barribob.boss.mixin;

import net.barribob.boss.Mod;
import net.barribob.boss.mob.Entities;
import net.barribob.boss.mob.mobs.lich.LichUtils;
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithUtils;
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomEntity;
import net.barribob.boss.render.NodeBossBarRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.world.BossEvent;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossBarHudMixin {

    @Shadow
    @Final
    private static Identifier[] BAR_BACKGROUND_SPRITES;
    @Shadow
    @Final
    private static Identifier[] BAR_PROGRESS_SPRITES;
    private static final NodeBossBarRenderer lichBossBarRenderer =
            new NodeBossBarRenderer(Entities.INSTANCE.getLICH().getDescriptionId(),
            LichUtils.INSTANCE.getHpPercentRageModes(),
                    Mod.INSTANCE.identifier("textures/gui/lich_boss_bar_dividers.png"),
                    LichUtils.textureSize);
    @Unique
    private static final NodeBossBarRenderer voidBlossomBarRenderer =
            new NodeBossBarRenderer(Entities.INSTANCE.getVOID_BLOSSOM().getDescriptionId(),
                    VoidBlossomEntity.Companion.getHpMilestones(),
                    Mod.INSTANCE.identifier("textures/gui/void_blossom_boss_bar_dividers.png"),
                    LichUtils.textureSize);

    @Inject(
            method = "extractBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/BossEvent;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderCustomBossBar(GuiGraphicsExtractor drawContext, int x, int y, BossEvent bossBar, CallbackInfo ci) {
        lichBossBarRenderer.renderBossBar(BAR_BACKGROUND_SPRITES, BAR_PROGRESS_SPRITES, drawContext, x, y, bossBar, ci);
        voidBlossomBarRenderer.renderBossBar(BAR_BACKGROUND_SPRITES, BAR_PROGRESS_SPRITES, drawContext, x, y, bossBar, ci);
        ObsidilithUtils.INSTANCE.getObsidilithBossBarRenderer().renderBossBar(BAR_BACKGROUND_SPRITES, BAR_PROGRESS_SPRITES, drawContext, x, y, bossBar, ci);
    }
}

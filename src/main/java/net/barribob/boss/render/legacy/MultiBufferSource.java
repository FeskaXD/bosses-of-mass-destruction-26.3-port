package net.barribob.boss.render.legacy;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

/** The 1.21 buffer source interface, kept for BOMD's 1.21 rendering code (see {@link CapturingBufferSource}). */
public interface MultiBufferSource {
    VertexConsumer getBuffer(RenderType renderType);
}

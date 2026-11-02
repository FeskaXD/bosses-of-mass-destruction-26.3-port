package net.barribob.boss.render.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bridges 1.21 immediate-mode rendering to 26.3's submit pipeline: old code writes vertices (already transformed by
 * its PoseStack) into these buffers, then {@link #flush()} hands each render type's vertices to the collector as
 * custom geometry, replayed with an identity pose.
 */
public class CapturingBufferSource implements MultiBufferSource {
    private final SubmitNodeCollector collector;
    private final Map<RenderType, Capture> captures = new LinkedHashMap<>();

    public CapturingBufferSource(SubmitNodeCollector collector) {
        this.collector = collector;
    }

    /** For legacy code that needs to submit something directly (items, text). */
    public SubmitNodeCollector collector() {
        return collector;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return captures.computeIfAbsent(renderType, t -> new Capture());
    }

    public void flush() {
        PoseStack identity = new PoseStack();
        for (Map.Entry<RenderType, Capture> e : captures.entrySet()) {
            Capture capture = e.getValue();
            if (capture.vertices.isEmpty()) continue;
            List<float[]> verts = new ArrayList<>(capture.vertices);
            collector.submitCustomGeometry(identity, e.getKey(), (pose, consumer) -> {
                for (float[] v : verts) {
                    consumer.addVertex(v[0], v[1], v[2])
                        .setColor(Float.floatToRawIntBits(v[3]))
                        .setUv(v[4], v[5])
                        .setUv1((int) v[6], (int) v[7])
                        .setUv2((int) v[8], (int) v[9])
                        .setNormal(v[10], v[11], v[12]);
                }
            });
        }
        captures.clear();
    }

    /** Records one vertex at a time: x y z color u v overlayU overlayV lightU lightV nx ny nz. */
    private static final class Capture implements VertexConsumer {
        final List<float[]> vertices = new ArrayList<>();
        private float[] cur;

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            cur = new float[]{x, y, z, Float.intBitsToFloat(-1), 0, 0, 0, 10, 240, 240, 0, 1, 0};
            vertices.add(cur);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return setColor((a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255));
        }

        @Override
        public VertexConsumer setColor(int argb) {
            if (cur != null) cur[3] = Float.intBitsToFloat(argb);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            if (cur != null) { cur[4] = u; cur[5] = v; }
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            if (cur != null) { cur[6] = u; cur[7] = v; }
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            if (cur != null) { cur[8] = u; cur[9] = v; }
            return this;
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            if (cur != null) { cur[10] = x; cur[11] = y; cur[12] = z; }
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }
}

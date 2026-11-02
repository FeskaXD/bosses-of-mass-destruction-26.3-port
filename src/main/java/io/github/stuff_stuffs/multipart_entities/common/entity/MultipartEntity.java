package io.github.stuff_stuffs.multipart_entities.common.entity;

import io.github.stuff_stuffs.multipart_entities.common.util.CompoundOrientedBox;
import net.minecraft.world.phys.AABB;

public interface MultipartEntity {
    CompoundOrientedBox getCompoundBoundingBox(AABB bounds);
}

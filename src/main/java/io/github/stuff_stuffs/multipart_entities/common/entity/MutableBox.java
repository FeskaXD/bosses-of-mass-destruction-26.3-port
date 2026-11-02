package io.github.stuff_stuffs.multipart_entities.common.entity;

import net.minecraft.world.phys.AABB;

public final class MutableBox {
    private AABB box;

    public MutableBox(AABB box) {
        this.box = box;
    }

    public AABB getBox() {
        return box;
    }

    public void setBox(AABB box) {
        this.box = box;
    }
}

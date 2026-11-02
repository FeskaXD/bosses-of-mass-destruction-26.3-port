package net.barribob.boss.block

import net.minecraft.util.StringRepresentable

enum class TripleBlockPart : StringRepresentable {
    TOP,
    MIDDLE,
    BOTTOM;

    override fun toString(): String {
        return serializedName
    }

    override fun getSerializedName(): String {
        return when {
            this == TOP -> "top"
            this == MIDDLE -> "middle"
            else -> "bottom"
        }
    }
}
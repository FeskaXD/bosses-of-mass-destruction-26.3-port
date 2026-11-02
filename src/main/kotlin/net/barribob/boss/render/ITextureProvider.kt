package net.barribob.boss.render

import net.minecraft.resources.Identifier

fun interface ITextureProvider <T> {
    fun getTexture(entity: T): Identifier
}
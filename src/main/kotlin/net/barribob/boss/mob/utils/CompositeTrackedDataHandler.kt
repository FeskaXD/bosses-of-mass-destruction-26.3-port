package net.barribob.boss.mob.utils

import net.minecraft.network.syncher.EntityDataAccessor

class CompositeTrackedDataHandler(vararg dataHandlers: ITrackedDataHandler) : ITrackedDataHandler {
    private val handlerList = dataHandlers.toList()

    override fun onTrackedDataSet(data: EntityDataAccessor<*>) {
        handlerList.forEach { it.onTrackedDataSet(data) }
    }
}
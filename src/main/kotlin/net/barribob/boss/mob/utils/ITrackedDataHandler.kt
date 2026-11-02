package net.barribob.boss.mob.utils

import net.minecraft.network.syncher.EntityDataAccessor

interface ITrackedDataHandler {
    fun onTrackedDataSet(data: EntityDataAccessor<*>)
}
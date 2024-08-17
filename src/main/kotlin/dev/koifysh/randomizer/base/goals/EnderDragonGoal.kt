package dev.koifysh.randomizer.base.goals

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APGoal

class EnderDragonGoal : APGoal() {

    init {
        type = ArchipelagoRandomizer.modResource("dragon_boss")
    }

    override fun start() {
    }

    override var isComplete: Boolean = false
    override var hasBossBar: Boolean = false
}
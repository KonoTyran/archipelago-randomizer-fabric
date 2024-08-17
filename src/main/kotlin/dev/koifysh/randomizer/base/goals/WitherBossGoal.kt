package dev.koifysh.randomizer.base.goals

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APGoal

class WitherBossGoal : APGoal() {

    init {
        type = ArchipelagoRandomizer.modResource("wither_boss")
    }

    override var hasBossBar: Boolean = false
    override var isComplete: Boolean = false

        override fun start() {
        TODO("Not yet implemented")
    }
}
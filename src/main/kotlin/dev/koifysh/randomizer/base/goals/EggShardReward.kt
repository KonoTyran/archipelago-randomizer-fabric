package dev.koifysh.randomizer.base.goals

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APItemReward

class EggShardReward : APItemReward() {

    override fun onItemObtain(index: Long) {
        ArchipelagoRandomizer.goalHandler.eggShardGoal?.checkCompletion()
    }
}
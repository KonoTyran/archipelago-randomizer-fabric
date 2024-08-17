package dev.koifysh.randomizer.base.goals

import dev.koifysh.randomizer.registries.APGoal

class BuiltInGoals {

    // store our built-in goals
    var advancementGoal: AdvancementGoal? = null; private set
    var eggShardGoal: EggShardGoal? = null; private set

    //store out built-in bosses
    var enderDirectionGoal: EnderDragonGoal? = null; private set
    var witherBossGoal: WitherBossGoal? = null; private set

    fun initializeGoal(goal: APGoal) {
        when (goal) {
            is AdvancementGoal -> advancementGoal = goal
            is EggShardGoal -> eggShardGoal = goal
            is EnderDragonGoal -> enderDirectionGoal = goal
            is WitherBossGoal -> witherBossGoal = goal
        }
    }
}

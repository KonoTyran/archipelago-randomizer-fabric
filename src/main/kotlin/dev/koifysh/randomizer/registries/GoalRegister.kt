package dev.koifysh.randomizer.registries

import dev.koifysh.archipelago.ClientStatus
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.registries.deserializers.APGoalDeserializer
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

class GoalRegister {

    private val goalCallbacks = HashMap<ResourceLocation, (APGoal) -> Unit>()
    private val goals = HashMap<ResourceLocation, APGoal>()

    private val goalRequirements = HashMap<ResourceLocation, ArrayList<ResourceLocation>>()

    fun <T : APGoal> register(type: ResourceLocation, location: Class<T>) {
        register(type, location, null)
    }

    fun <T : APGoal> register(type: ResourceLocation, location: Class<T>, consumer: ((APGoal) -> Unit)?) {
        if (APGoalDeserializer.register(type, location)) {
            if (consumer != null) {
                goalCallbacks[type] = consumer
            }
        } else {
            logger.error("attempted to register duplicate goal $type, skipping")
        }
    }

    fun goalCompleted(completedGoal: APGoal) {
        if (!completedGoal.isComplete) return // sanity check

        goalRequirements.forEach { (id, requirements) ->
            if (requirements.contains(completedGoal.id)) {
                goals[id]?.checkRequirementCompletion() // inform all goals that have this goal as a requirement to check if they are now complete
            }
        }

        if (goals.values.all { it.isComplete }) {
            ArchipelagoRandomizer.apClient.setGameState(ClientStatus.CLIENT_GOAL)
        }
    }

    internal fun newGoal(goal: APGoal): Int {
        try {
            goalCallbacks[goal.type]?.invoke(goal)
            goalRequirements[goal.id] = ArrayList(goal.requirements)
            goals[goal.id] = goal
            return 1
        } catch (e: Exception) {
            logger.error("Error while processing goal ${goal.type}. ${e.message}")
        }
        return 0
    }

    internal fun initializeGoals() {
        // call prepareStart on all goals that have no requirements
        goals.forEach { (id, goal) ->
            if (goalRequirements[id]!!.isEmpty()) {
                goal.prepareStart()
            }
        }
    }

    fun isEmpty(): Boolean {
        return goals.isEmpty()
    }


    fun getGoalsThatRequire(id: ResourceLocation): List<APGoal> {
        return goalRequirements[id]?.filter { goals.containsKey(it) }?.map { goals[it]!! } ?: emptyList()
    }

    fun addPlayerToBossBar(player: ServerPlayer) {
        goals.values.forEach { it.addPlayerToBossBar(player) }

    }
}
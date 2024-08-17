package dev.koifysh.randomizer.registries

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
        if (APGoalDeserializer.register(type, location))
            if (consumer != null)
                goalCallbacks[type] = consumer
        else
            logger.error("attempted to register duplicate goal $type, skipping")
    }

    fun goalCompleted(completedGoal: APGoal) {
        if (!completedGoal.isComplete) return // sanity check!

        goalRequirements.forEach { (resourceLocation, requirements) ->
            if (requirements.contains(completedGoal.type)) {
                goals[resourceLocation]?.checkRequirementCompletion() // inform all goals that have this goal as a requirement to check if they are now complete
            }
        }
    }

    internal fun newGoal(goal: APGoal): Int {
        try {
            goalCallbacks[goal.type]?.invoke(goal)
            goalRequirements[goal.type] = ArrayList(goal.requirements)
            goals[goal.type] = goal
            return 1
        } catch (e: Exception) {
            logger.error("Error while processing goal ${goal.type}. ${e.message}")
        }
        return 0
    }

    internal fun initializeGoals() {
        // call prepareStart on all goals that have no requirements
        goals.forEach { (location, goal) ->
            goalRequirements[location]?.isEmpty()?.let { goal.prepareStart() }
        }
    }

    fun isEmpty(): Boolean {
        return goals.isEmpty()
    }


    fun getGoalsThatRequire(type: ResourceLocation): List<APGoal> {
       return goalRequirements[type]?.filter { goals.containsKey(it) }?.map { goals[it]!! } ?: emptyList()
    }

    fun addPlayerToBossBar(player: ServerPlayer) {
        goals.values.forEach { it.addPlayerToBossBar(player) }

    }
}
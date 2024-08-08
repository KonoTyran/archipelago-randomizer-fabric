package dev.koifysh.randomizer.data.recipes

import net.minecraft.world.item.crafting.RecipeHolder
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger

class RecipeRewards<APItemReward> {

    val recipes: ArrayList<RecipeHolder<*>> = ArrayList()
    private val uninitializedRewards = ArrayList<APRecipe>()

    fun registerRecipe(reward: APItemReward) {
        if (reward !is APRecipe) return

        uninitializedRewards.add(reward)
    }

    fun initialize() {
        logger.info("Initializing ${uninitializedRewards.size} rewards")
        uninitializedRewards.forEach { reward ->
            recipes.addAll(reward.getGrantedRecipes())
        }
        uninitializedRewards.clear()
        logger.info("Initialized ${recipes.size} recipes")
    }
}
package dev.koifysh.randomizer.data.recipes

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.ArchipelagoRandomizer.server
import dev.koifysh.randomizer.registries.APItemReward
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeHolder

data class ProgressiveRecipe(
    @SerializedName("tracking-advancement")
    val trackingAdvancement: ResourceLocation,

    @SerializedName("recipes")
    val items: List<List<ResourceLocation>>,
) : APRecipe, APItemReward() {

    override fun getGrantedRecipes(): Set<RecipeHolder<*>> {
        val recipes = HashSet<RecipeHolder<*>>()
        items.forEach { group ->
            group.forEach { recipe ->
                val recipeOptional = server.recipeManager.byKey(recipe)
                recipeOptional.ifPresent { holder ->
                    recipes.add(holder)
                }
            }
        }
        return recipes
    }

    override fun getTrackingAdvancements(): Set<ResourceLocation> {
        return setOf(trackingAdvancement)
    }

    override fun grant(index: Long) {
        server.playerList.players.forEach {player ->
            player.awardRecipes(getGrantedRecipes())
        }
    }

}

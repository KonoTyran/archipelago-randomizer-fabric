package dev.koifysh.randomizer.data.recipes

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.ArchipelagoRandomizer
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

    private var tier : Int = 0

    override fun getGrantedRecipes(): Set<RecipeHolder<*>> {
        val recipes = HashSet<RecipeHolder<*>>()
        items
            .filterIndexed { index, _ -> tier > index }
            .forEach { recipeList ->
                recipeList.forEach { recipe ->
                    val recipeOptional = server.recipeManager.byKey(recipe)
                    recipeOptional.ifPresent { holder ->
                        recipes.add(holder)
                }
            }
        }
        return recipes
    }

    override fun getTrackingAdvancements(): Set<ResourceLocation> {
        val advancements = HashSet<ResourceLocation>()
        for (i in items.indices)
            advancements.add(ResourceLocation.parse(trackingAdvancement.toString() + "_$i"))
        return advancements
    }

    override fun getAllRecipes(): Set<RecipeHolder<*>> {
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

    override fun grant(index: Long) {
        tier++
        server.playerList.players.forEach {player ->
            player.awardRecipes(getGrantedRecipes())
        }
        ArchipelagoRandomizer.recipeHandler.track(getTrackingAdvancements())
    }

}
package dev.koifysh.randomizer.rewards.recipes

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.server
import dev.koifysh.randomizer.registries.APItemReward
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.crafting.RecipeHolder

data class GroupRecipe(
    @SerializedName("tracking-advancement")
    val trackingAdvancement: ResourceLocation,

    @SerializedName("recipes")
    val items: List<ResourceLocation>,
) : APRecipe, APItemReward() {

    override fun getGrantedRecipes(): Set<RecipeHolder<*>> {
        val recipes = HashSet<RecipeHolder<*>>()
        items.forEach {
            val recipeOptional = server.recipeManager.byKey(it)
            recipeOptional.ifPresent { holder ->
                recipes.add(holder)
            }
        }
        return recipes
    }

    override fun getTrackingAdvancements(): Set<ResourceLocation> {
        return setOf(trackingAdvancement)
    }

    override fun getAllRecipes() = getGrantedRecipes()

    override fun onItemObtain(index: Long) {
        val toGrant = getGrantedRecipes()
        ArchipelagoRandomizer.recipeHandler.add(toGrant)
        server.playerList.players.forEach { player ->
            player.awardRecipes(toGrant)
        }
        ArchipelagoRandomizer.recipeHandler.track(getTrackingAdvancements())
    }

    override fun grantPlayer(player: ServerPlayer, index: Long) {
        // NO-OP
    }

}

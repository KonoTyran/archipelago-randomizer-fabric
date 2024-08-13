package dev.koifysh.randomizer.rewards.recipes

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeHolder

interface APRecipe {

    fun getGrantedRecipes(): Set<RecipeHolder<*>>

    fun getTrackingAdvancements(): Set<ResourceLocation>

    fun getAllRecipes(): Set<RecipeHolder<*>>

}
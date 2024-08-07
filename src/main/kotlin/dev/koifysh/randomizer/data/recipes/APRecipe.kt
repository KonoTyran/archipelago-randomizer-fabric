package dev.koifysh.randomizer.data.recipes

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeHolder

interface APRecipe {

    fun getGrantedRecipes(): Set<RecipeHolder<*>>

    fun getTrackingAdvancements(): Set<ResourceLocation>

}
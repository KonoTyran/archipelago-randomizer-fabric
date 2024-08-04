package dev.koifysh.randomizer.ktmixin

import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.RecipeHolder
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.*

object KTMixinCrafter {

    fun getPotentialResults(
        cir: CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>>,
    ) {
        cir.returnValue = cir.returnValue
    }
}
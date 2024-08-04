package dev.koifysh.mixin;


import dev.koifysh.randomizer.ktmixin.KTMixinCrafter;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrafterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;


@Mixin(CrafterBlock.class)
public class MixinCrafter {

    @Inject(method = "getPotentialResults", at = @At("RETURN"), cancellable = true)
    private static void getPotentialResults(Level level, CraftingInput craftingInput, CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir) {
        KTMixinCrafter.INSTANCE.getPotentialResults(cir);
    }
}

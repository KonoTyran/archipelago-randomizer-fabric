package dev.koifysh.mixin;

import dev.koifysh.randomizer.ArchipelagoRandomizer;
import dev.koifysh.randomizer.utils.Utils;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
    private void onInit(ItemLike itemLike, int i, PatchedDataComponentMap patchedDataComponentMap, CallbackInfo ci) {
        if (itemLike == Items.WITHER_SKELETON_SKULL) {
            ArrayList<String> lore = new ArrayList<>();
            if (ArchipelagoRandomizer.INSTANCE.getGoalHandler().getWitherBossGoal() != null) {
                lore.add("Vibrates with power...");
            } else {
                lore.add("lies dormant...");
            }

            Utils.INSTANCE.setItemLore((ItemStack) (Object) this, lore);
        } else if (itemLike == Items.ENDER_PEARL) {
            ArrayList<String> lore = new ArrayList<>();
            if (ArchipelagoRandomizer.INSTANCE.getGoalHandler().getEnderDragonGoal() != null) {
                lore.add("Vibrates with power...");
            } else {
                lore.add("lies dormant...");
            }
            Utils.INSTANCE.setItemLore((ItemStack) (Object) this, lore);
        }
    }
}

package dev.koifysh.mixin;

import dev.koifysh.randomizer.ktmixin.KTMixinCompass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class MixinUpdateCompass {

    @Inject(method = "useItem", at = @At("HEAD"))
    private void onUseCompass(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        KTMixinCompass.INSTANCE.cycleCompass(serverPlayer, level, itemStack);
    }
}

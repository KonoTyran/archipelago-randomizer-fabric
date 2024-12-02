package dev.koifysh.mixin;

import dev.koifysh.randomizer.ArchipelagoRandomizer;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinModInit {

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;startTimerHackThread()V", shift = At.Shift.AFTER))
    private static void afterModInit(String[] strings, CallbackInfo ci) {
        ArchipelagoRandomizer.INSTANCE.loadAPMC();
    }

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/Bootstrap;bootStrap()V"))
    private static void beforeModInit(String[] strings, CallbackInfo ci) {
        ArchipelagoRandomizer.INSTANCE.getLogger().info("pre-init setup");
    }
}

package dev.koifysh.mixin;

import dev.koifysh.randomizer.ArchipelagoRandomizer;
import dev.koifysh.randomizer.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndDragonFight.class)
public abstract class MixinDragonFight {

    @Shadow
    private boolean previouslyKilled;

    @Inject(method = "setDragonKilled(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerBossEvent;setProgress(F)V"
            )
    )
    private void onDragonKilled(EnderDragon enderDragon, CallbackInfo ci) {
        previouslyKilled = false;
        var dragonGoal = ArchipelagoRandomizer.INSTANCE.getGoalHandler().getEnderDragonGoal();
        if (dragonGoal != null) {
            if (dragonGoal.isComplete() && dragonGoal.getHasStarted()) return;
            Utils.INSTANCE.sendMessageToAll(Component.literal("She is no more...").withStyle(ChatFormatting.GOLD));
            dragonGoal.setComplete(true);
            dragonGoal.checkCompletion();

        }
    }

    @Shadow
    protected abstract void spawnNewGateway();

    @Inject(method = "createNewDragon()Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;", at = @At("RETURN"))
    private void spawnNewDragon(CallbackInfoReturnable<EnderDragon> cir) {
        for (int i = 0; i < 20; i++) {
            spawnNewGateway();
        }
    }
}

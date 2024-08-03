package dev.koifysh.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.koifysh.randomizer.ArchipelagoRandomizer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerAdvancements.class)
public class AdvancementGrant {

	@Shadow private ServerPlayer player;

	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), method = "award(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z")
	private void onAdvancementGrant(AdvancementHolder advancementHolder, String progress, CallbackInfoReturnable<Boolean> cir) {
		ArchipelagoRandomizer.INSTANCE.getAdvancementLocations().onAdvancementGrant(advancementHolder, this.player);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementProgress;isDone()Z", ordinal = 2), method = "award(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z")
	private void onAdvancementProgress(AdvancementHolder advancementHolder, String progress, CallbackInfoReturnable<Boolean> cir, @Local AdvancementProgress advancementProgress, @Share("grantProgress")LocalBooleanRef grantProgress) {
		if (grantProgress.get())
			ArchipelagoRandomizer.INSTANCE.getAdvancementLocations().onAdvancementProgress(advancementHolder, advancementProgress);
	}

	@ModifyExpressionValue(method = "award(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementProgress;grantProgress(Ljava/lang/String;)Z"))
	private boolean captureValue(boolean grantProgress, @Share("grantProgress")LocalBooleanRef shared) {
		shared.set(grantProgress);
		return grantProgress;
	}


}
package dev.koifysh.mixin;


import dev.koifysh.randomizer.utils.Utils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerAdvancements.class)
public class AdvancementGrant {
	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), method = "award(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z")
	private void onAdvancementGrant(AdvancementHolder advancementHolder, String string, CallbackInfoReturnable<Boolean> cir) {
		advancementHolder.value().display().ifPresent((it -> Utils.INSTANCE.sendMessageToAll("Advancement granted: " + it.getTitle().getString())));
	}
}
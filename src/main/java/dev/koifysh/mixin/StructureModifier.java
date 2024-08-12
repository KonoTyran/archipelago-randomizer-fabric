package dev.koifysh.mixin;


import dev.koifysh.randomizer.ktmixin.KTMixinStructures;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MinecraftServer.class)
public class StructureModifier {

	@Inject(at = @At(value = "HEAD"), method = "loadLevel")
	private void onAdvancementGrant(CallbackInfo ci) {
		KTMixinStructures.INSTANCE.randomizeStructures();
	}
}
package dev.koifysh.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hoglin.class)
public class MixinHoglin {

    @Inject(method = "isConverting()Z", at = @At("RETURN"), cancellable = true)
    private void isConverting(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        Hoglin hoglin = (Hoglin) (Object) this;
        if (!(hoglin.level() instanceof ServerLevel serverLevel)) return;

        var structureRegistry = serverLevel.registryAccess().registry(Registries.STRUCTURE);
        if (structureRegistry.isEmpty()) return;
        var bastion = structureRegistry.get().get(ResourceLocation.parse("minecraft:bastion_remnant"));
        if (serverLevel.structureManager().getStructureWithPieceAt(hoglin.blockPosition(), bastion).isValid()) {
            cir.setReturnValue(false);
        }

    }
}

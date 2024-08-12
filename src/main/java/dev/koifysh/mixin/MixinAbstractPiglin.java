package dev.koifysh.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractPiglin.class)
public class MixinAbstractPiglin {

    @Inject(method = "isConverting()Z", at = @At("RETURN"), cancellable = true)
    private void isConverting(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        AbstractPiglin pig = (AbstractPiglin) (Object) this;
        if (!(pig.level() instanceof ServerLevel serverLevel)) return;

        var structureRegistry = serverLevel.registryAccess().registry(Registries.STRUCTURE);
        if (structureRegistry.isEmpty()) return;
        var bastion = structureRegistry.get().get(ResourceLocation.parse("minecraft:bastion_remnant"));
        if (serverLevel.structureManager().getStructureWithPieceAt(pig.blockPosition(), bastion).isValid()) {
            cir.setReturnValue(false);
        }

    }
}

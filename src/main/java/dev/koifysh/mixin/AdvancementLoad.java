package dev.koifysh.mixin;

import com.google.common.collect.ImmutableMap;
import dev.koifysh.randomizer.ktmixin.KTMixinAdvancementLoad;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;;


@Mixin(ServerAdvancementManager.class)
public class AdvancementLoad {
    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/collect/ImmutableMap$Builder;buildOrThrow()Lcom/google/common/collect/ImmutableMap;"
    ),
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
    private ImmutableMap<ResourceLocation, AdvancementHolder> onAdvancementLoad(ImmutableMap.Builder<ResourceLocation, AdvancementHolder> old) {
        return KTMixinAdvancementLoad.INSTANCE.onAdvancementLoad(old);
    }

}

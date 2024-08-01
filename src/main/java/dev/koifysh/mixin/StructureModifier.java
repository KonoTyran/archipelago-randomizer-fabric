package dev.koifysh.mixin;


import dev.koifysh.randomizer.ArchipelagoRandomizer;
import dev.koifysh.randomizer.structure.ArchipelagoStructures;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MinecraftServer.class)
public class StructureModifier {
    private Logger LOGGER = LoggerFactory.getLogger(ArchipelagoRandomizer.MOD_ID);
	@Inject(at = @At(value = "HEAD"), method = "loadLevel")
	private void onAdvancementGrant(CallbackInfo ci) {
		ArchipelagoRandomizer.INSTANCE.getServer().registryAccess().registryOrThrow(Registries.STRUCTURE).holders().forEach((structure) -> {

		ArchipelagoRandomizer.INSTANCE.getLogger().debug("Altering biome list for {}", structure.unwrapKey().get().location());

        HolderSet<Biome> biomes = structure.value().biomes();

        switch (structure.unwrapKey().get().location().toString()) {
            case "minecraft:village_plains", "minecraft:village_desert", "minecraft:village_savanna", "minecraft:village_snowy", "minecraft:village_taiga" -> {
                if (!ArchipelagoStructures.Companion.getStructures().get("Village").equals(ArchipelagoStructures.Companion.getOverworldStructures()))
                    biomes = noBiomes;
            }
            case "aprandomizer:village_nether" -> {
                if (!structures.get("Village").equals(overworldStructures))
                    biomes = structures.get("Village");
            }
            case "minecraft:pillager_outpost" -> {
                if (!structures.get("Pillager Outpost").equals(overworldStructures))
                    biomes = noBiomes;
            }
            case "aprandomizer:pillager_outpost_nether" -> {
                if (!structures.get("Pillager Outpost").equals(overworldStructures))
                    biomes = structures.get("Pillager Outpost");
            }
            case "minecraft:fortress" -> biomes = structures.get("Nether Fortress");
            case "minecraft:bastion_remnant" -> biomes = structures.get("Bastion Remnant");
            case "minecraft:end_city" -> {
                if (structures.get("End City").equals(netherStructures))
                    biomes = noBiomes;
                else if (!structures.get("End City").equals(endStructures))
                    biomes = structures.get("End City");
            }
            case "aprandomizer:end_city_nether" -> {
                if (structures.get("End City").equals(netherStructures))
                    biomes = structures.get("End City");
            }
        }

		});
	}
}
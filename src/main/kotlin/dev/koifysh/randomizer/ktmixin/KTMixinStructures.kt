package dev.koifysh.randomizer.ktmixin

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.apmcData
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.ArchipelagoRandomizer.server
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.structure.Structure

object KTMixinStructures {
    val structures: HashMap<String, HolderSet<Biome>> = HashMap()

    fun randomizeStructures() {
        val biomeRegistry: Registry<Biome> = server.registryAccess().registryOrThrow(Registries.BIOME)

        val overworldTag: TagKey<Biome> = TagKey.create(Registries.BIOME, ArchipelagoRandomizer.modResource("overworld_structure"))
        val netherTag: TagKey<Biome> = TagKey.create(Registries.BIOME, ArchipelagoRandomizer.modResource("nether_structure"))
        val endTag: TagKey<Biome> = TagKey.create(Registries.BIOME, ArchipelagoRandomizer.modResource("end_structure"))
        val noneTag: TagKey<Biome> = TagKey.create(Registries.BIOME, ArchipelagoRandomizer.modResource("none"))

        val overworldBiomes: HolderSet<Biome> = biomeRegistry.getTag(overworldTag).orElseThrow()
        val netherBiomes: HolderSet<Biome> = biomeRegistry.getTag(netherTag).orElseThrow()
        val endBiomes: HolderSet<Biome> = biomeRegistry.getTag(endTag).orElseThrow()
        val noBiomes: HolderSet<Biome> = biomeRegistry.getTag(noneTag).orElseThrow()

        if (structures.isNotEmpty()) return
            logger.info("Loading Tags and Biome info.")

            for ((key, value) in apmcData.structures) {
                when (key) {
                    "Overworld Structure 1", "Overworld Structure 2" -> structures[value] = overworldBiomes
                    "Nether Structure 1", "Nether Structure 2" -> structures[value] = netherBiomes
                    "The End Structure" -> structures[value] = endBiomes
                }
            }

        server.registryAccess().registryOrThrow(Registries.STRUCTURE).holders()
            .forEach { holder ->
                holder.value()
                logger.debug("Altering biome list for {}", holder.key().location())
                var biomes = holder.value().biomes()
                when (holder.key().location().toString()) {
                    "minecraft:village_plains", "minecraft:village_desert", "minecraft:village_savanna", "minecraft:village_snowy", "minecraft:village_taiga" -> {
                        if (structures["Village"] != overworldBiomes) biomes = noBiomes
                    }

                    ArchipelagoRandomizer.MOD_ID + ":village_nether" -> {
                        if (structures["Village"]!! != overworldBiomes) biomes = structures["Village"]
                    }

                    "minecraft:pillager_outpost" -> {
                        if (structures["Pillager Outpost"]!! != overworldBiomes) biomes = noBiomes
                    }

                    ArchipelagoRandomizer.MOD_ID + ":pillager_outpost_nether" -> {
                        if (structures["Pillager Outpost"]!! != overworldBiomes) biomes = structures["Pillager Outpost"]
                    }

                    "minecraft:fortress" -> biomes = structures["Nether Fortress"]
                    "minecraft:bastion_remnant" -> biomes = structures["Bastion Remnant"]
                    "minecraft:end_city" -> {
                        if (structures["End City"]!! == netherBiomes) biomes = noBiomes
                        else if (structures["End City"]!! != endBiomes) biomes = structures["End City"]
                    }

                    ArchipelagoRandomizer.MOD_ID + "end_city_nether" -> {
                        if (structures["End City"]!! == netherBiomes) biomes = structures["End City"]
                    }
                }
                val structure = holder.value()
                structure.settings = Structure.StructureSettings.Builder(biomes)
                    .generationStep(structure.step())
                    .spawnOverrides(structure.spawnOverrides())
                    .terrainAdapation(structure.terrainAdaptation())
                    .build()
            }
    }

}
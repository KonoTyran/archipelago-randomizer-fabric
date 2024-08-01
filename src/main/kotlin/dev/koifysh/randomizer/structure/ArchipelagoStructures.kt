package dev.koifysh.randomizer.structure

import com.mojang.serialization.MapCodec
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.apmcData
import dev.koifysh.randomizer.ArchipelagoRandomizer.server
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class ArchipelagoStructures {

    companion object {
        private val biomeRegistry: Registry<Biome> = server.registryAccess().registryOrThrow(Registries.BIOME)
        private val overworldTag: TagKey<Biome> = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID, "overworld_structure")
        )
        private val netherTag: TagKey<Biome> = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID, "nether_structure")
        )
        private val endTag: TagKey<Biome> = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID, "end_structure")
        )
        private val noneTag: TagKey<Biome> = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID, "none"))

        val overworldStructures: HolderSet<Biome> = biomeRegistry.getTag(overworldTag).orElseThrow()
        val netherStructures: HolderSet<Biome> = biomeRegistry.getTag(netherTag).orElseThrow()
        val endStructures: HolderSet<Biome> = biomeRegistry.getTag(endTag).orElseThrow()
        val noBiomes: HolderSet<Biome> = biomeRegistry.getTag(noneTag).orElseThrow()
        public val structures: HashMap<String, HolderSet<Biome>> = HashMap()

        lateinit var END_CITY_NETHER: StructureType<NetherEndCityStructure>
        lateinit var BEE_GROVE: StructureType<NetherVillageStructure>
        lateinit var PILLAGER_OUTPOST_NETHER: StructureType<NetherPillagerOutpostStructure>
        lateinit var VILLAGE_NETHER: StructureType<NetherVillageStructure>

        val VILLAGE_TAG: TagKey<Structure> = TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID,"village"))
        val OUTPOST_TAG: TagKey<Structure> = TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID,"pillager_outpost"))
        val END_CITY_TAG: TagKey<Structure> = TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID,"end_city"))
        val BASTION_REMNANT_TAG: TagKey<Structure> = TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID,"bastion_remnant"))
        val FORTRESS_TAG: TagKey<Structure> = TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID,"fortress"))
    }

    val logger: Logger = LoggerFactory.getLogger("archipelago-randomizer")

    private fun loadTags() {
        if (structures.isNotEmpty()) return
        logger.info("Loading Tags and Biome info.")

        val data = apmcData
        for ((key, value) in data.structures) {
            when (key) {
                "Overworld Structure 1", "Overworld Structure 2" -> structures[value] = overworldStructures
                "Nether Structure 1", "Nether Structure 2" -> structures[value] = netherStructures
                "The End Structure" -> structures[value] = endStructures
            }
        }
    }

    fun registerStructures() {
        loadTags()
        END_CITY_NETHER = register(ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID, "end_city_nether"),NetherEndCityStructure.CODEC )
        BEE_GROVE = register(ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID, "bee_grove"), NetherVillageStructure.CODEC)
        VILLAGE_NETHER = register(ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID, "village_nether"), NetherVillageStructure.CODEC)
        PILLAGER_OUTPOST_NETHER = register(ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID, "pillager_outpost_nether"), NetherPillagerOutpostStructure.CODEC)
    }

    private fun <S : Structure> register(location: ResourceLocation, mapCodec: MapCodec<S>): StructureType<S> {
        return Registry.register(BuiltInRegistries.STRUCTURE_TYPE, location, StructureType { mapCodec })
    }

}
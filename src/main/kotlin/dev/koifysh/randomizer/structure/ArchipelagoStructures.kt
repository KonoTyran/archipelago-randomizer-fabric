package dev.koifysh.randomizer.structure

import com.mojang.serialization.MapCodec
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType


object ArchipelagoStructures {

//

//
//        val VILLAGE_TAG: TagKey<Structure> = TagKey.create(Registries.STRUCTURE, ArchipelagoRandomizer.modResource("village"))
//        val OUTPOST_TAG: TagKey<Structure> = TagKey.create(Registries.STRUCTURE, ArchipelagoRandomizer.modResource("pillager_outpost"))
//        val END_CITY_TAG: TagKey<Structure> = TagKey.create(Registries.STRUCTURE, ArchipelagoRandomizer.modResource("end_city"))
//        val BASTION_REMNANT_TAG: TagKey<Structure> = TagKey.create(Registries.STRUCTURE, ArchipelagoRandomizer.modResource("bastion_remnant"))
//        val FORTRESS_TAG: TagKey<Structure> = TagKey.create(Registries.STRUCTURE, ArchipelagoRandomizer.modResource("fortress"))
//        val BEE_GROVE_TAG: TagKey<Structure> = TagKey.create(Registries.STRUCTURE, ArchipelagoRandomizer.modResource("bee_grove"))


    lateinit var END_CITY_NETHER: StructureType<NetherEndCityStructure>
    lateinit var BEE_GROVE: StructureType<NetherVillageStructure>
    lateinit var PILLAGER_OUTPOST_NETHER: StructureType<NetherPillagerOutpostStructure>
    lateinit var VILLAGE_NETHER: StructureType<NetherVillageStructure>

    fun registerStructures() {
        logger.info("Registering structures")
        logger.debug("Registering End City Nether")
        END_CITY_NETHER = register(ArchipelagoRandomizer.modResource("end_city_nether"), NetherEndCityStructure.CODEC)
        logger.debug("Registering Bee Grove")
        BEE_GROVE = register(ArchipelagoRandomizer.modResource("bee_grove"), NetherVillageStructure.CODEC)
        logger.debug("Registering Nether Village")
        VILLAGE_NETHER = register(ArchipelagoRandomizer.modResource("village_nether"), NetherVillageStructure.CODEC)
        logger.debug("Registering Pillager Outpost Nether")
        PILLAGER_OUTPOST_NETHER = register(ArchipelagoRandomizer.modResource("pillager_outpost_nether"), NetherPillagerOutpostStructure.CODEC)
        logger.info("Finished registering structures")
    }

    private fun <S : Structure> register(location: ResourceLocation, mapCodec: MapCodec<S>): StructureType<S> {
        return Registry.register(BuiltInRegistries.STRUCTURE_TYPE, location, StructureType { mapCodec })
    }
}
package dev.koifysh.randomizer.structure

import com.google.common.collect.Lists
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructurePiece
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder
import net.minecraft.world.level.levelgen.structure.structures.EndCityPieces
import java.util.*
import java.util.function.Consumer

class NetherEndCityStructure(config: StructureSettings) : Structure(config) {
    public override fun findGenerationPoint(context: GenerationContext): Optional<GenerationStub> {
        // get sea Level, or in our case lava level.
        val seaLevel = context.chunkGenerator().seaLevel

        val blockpos = context.chunkPos().getMiddleBlockPosition(seaLevel)

        return Optional.of(GenerationStub(blockpos) { structure: StructurePiecesBuilder ->
            val rotation = Rotation.getRandom(context.random())
            val list: List<StructurePiece> = Lists.newArrayList()
            EndCityPieces.startHouseTower(
                context.structureTemplateManager(),
                blockpos,
                rotation,
                list,
                context.random()
            )
            list.forEach(Consumer { structurePiece: StructurePiece? -> structure.addPiece(structurePiece) })
        })
    }

    override fun type(): StructureType<*> {
        return ArchipelagoStructures.END_CITY_NETHER
    }

    companion object {
        val CODEC: MapCodec<NetherEndCityStructure> =
            RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<NetherEndCityStructure> ->
                instance.group(
                    settingsCodec(instance)
                ).apply(instance) { config: StructureSettings -> NetherEndCityStructure(config) }
            }
    }
}
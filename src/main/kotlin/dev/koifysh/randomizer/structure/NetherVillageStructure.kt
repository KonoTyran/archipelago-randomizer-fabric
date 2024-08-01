package dev.koifysh.randomizer.structure

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.EmptyBlockGetter
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.WorldGenerationContext
import net.minecraft.world.level.levelgen.WorldgenRandom
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings
import java.util.*

class NetherVillageStructure(
    config: StructureSettings,
    val startPool: Holder<StructureTemplatePool>,
    val startJigsawName: Optional<ResourceLocation>,
    val size: Int,
    val startHeight: HeightProvider,
    val projectStartToHeightmap: Optional<Heightmap.Types>,
    val maxDistanceFromCenter: Int,
    val dimensionPadding: DimensionPadding,
    val liquidSettings: LiquidSettings
) : Structure(config) {

    public override fun findGenerationPoint(context: GenerationContext): Optional<GenerationStub> {
        // Check if the spot is valid for our structure. This is just as another method for cleanness.
        // Returning an empty optional tells the game to skip this spot as it will not generate the structure.

        if (!extraSpawningChecks(context)) {
            return Optional.empty()
        }

        val worldgenRandom: WorldgenRandom = context.random()
        val x: Int = context.chunkPos().minBlockX + worldgenRandom.nextInt(16)
        val z: Int = context.chunkPos().minBlockZ + worldgenRandom.nextInt(16)
        val seaLevel = context.chunkGenerator().seaLevel
        val worldGenerationContext =
            WorldGenerationContext(context.chunkGenerator(), context.heightAccessor())
        var y: Int = startHeight.sample(worldgenRandom, worldGenerationContext)
        val noiseColumn: NoiseColumn =
            context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState())
        val mutableBlockPos: BlockPos.MutableBlockPos = BlockPos.MutableBlockPos(x, y, z)

        while (y > seaLevel) {
            val blockState: BlockState = noiseColumn.getBlock(y)
            --y
            val blockState1: BlockState = noiseColumn.getBlock(y)
            if (blockState.isAir && (blockState1.`is`(Blocks.SOUL_SAND) || blockState1.isFaceSturdy(
                    EmptyBlockGetter.INSTANCE,
                    mutableBlockPos.setY(y),
                    Direction.UP
                ))
            ) {
                break
            }
        }
        if (y <= seaLevel) {
            return Optional.empty()
        }
        val blockPos = BlockPos(x, y, z)
        val structurePiecesGenerator: Optional<GenerationStub> =
            JigsawPlacement.addPieces(
                context,  // Used for JigsawPlacement to get all the proper behaviors done.
                this.startPool,  // The starting pool to use to create the structure layout from
                this.startJigsawName,  // Can be used to only spawn from one Jigsaw block. But we don't need to worry about this.
                this.size,  // How deep a branch of pieces can go away from center piece. (5 means branches cannot be longer than 5 pieces from center piece)
                blockPos,  // Where to spawn the structure.
                false,  // "useExpansionHack" This is for legacy villages to generate properly. You should keep this false always.
                this.projectStartToHeightmap,  // Adds the terrain height's y value to the passed in blockpos's y value. (This uses WORLD_SURFACE_WG heightmap which stops at top water too)
                // Here, blockpos's y value is 60 which means the structure spawn 60 blocks above terrain height.
                // Set this to false for structure to be place only at the passed in blockpos's Y value instead.
                // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
                this.maxDistanceFromCenter,  // Maximum limit for how far pieces can spawn from center. You cannot set this bigger than 128 or else pieces gets cutoff.
                PoolAliasLookup.EMPTY,  // Optional thing that allows swapping a template pool with another per structure json instance. We don't need this but see vanilla JigsawStructure class for how to wire it up if you want it.
                this.dimensionPadding,  // Optional thing to prevent generating too close to the bottom or top of the dimension.
                this.liquidSettings
            ) // Optional thing to control whether the structure will be waterlogged when replacing pre-existing water in the world.

        /*
         * Note, you are always free to make your own StructurePoolBasedGenerator class and implementation of how the structure
         * should generate. It is tricky but extremely powerful if you are doing something that vanilla's jigsaw system cannot do.
         * Such as for example, forcing 3 pieces to always spawn every time, limiting how often a piece spawns, or remove the intersection limitation of pieces.
         */

        // Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.
        return structurePiecesGenerator
    }

    override fun type(): StructureType<*> {
        return ArchipelagoStructures.VILLAGE_NETHER
    }

    companion object {
        val CODEC: MapCodec<NetherVillageStructure> =
            RecordCodecBuilder.mapCodec {
                instance: RecordCodecBuilder.Instance<NetherVillageStructure> ->
                instance.group<StructureSettings, Holder<StructureTemplatePool>, Optional<ResourceLocation>, Int, HeightProvider, Optional<Heightmap.Types>, Int, DimensionPadding, LiquidSettings>(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter { structure: NetherVillageStructure -> structure.startPool },
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter { structure: NetherVillageStructure -> structure.startJigsawName },
                    Codec.intRange(0, 30).fieldOf("size").forGetter { structure: NetherVillageStructure -> structure.size },
                    HeightProvider.CODEC.fieldOf("start_height").forGetter { structure: NetherVillageStructure -> structure.startHeight },
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter { structure: NetherVillageStructure -> structure.projectStartToHeightmap },
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter { structure: NetherVillageStructure -> structure.maxDistanceFromCenter },
                    DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter { structure: NetherVillageStructure -> structure.dimensionPadding },
                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter { structure: NetherVillageStructure -> structure.liquidSettings }
                ).apply(
                    instance
                ) { config: StructureSettings, startPool: Holder<StructureTemplatePool>, startJigsawName: Optional<ResourceLocation>, size: Int, startHeight: HeightProvider, projectStartToHeightmap: Optional<Heightmap.Types>, maxDistanceFromCenter: Int, dimensionPadding: DimensionPadding, liquidSettings: LiquidSettings ->
                    NetherVillageStructure(
                        config,
                        startPool,
                        startJigsawName,
                        size,
                        startHeight,
                        projectStartToHeightmap,
                        maxDistanceFromCenter,
                        dimensionPadding,
                        liquidSettings
                    )
                }
            }

        /*
     * This is where extra checks can be done to determine if the structure can spawn here.
     * This only needs to be overridden if you're adding additional spawn conditions.
     *
     * Fun fact, if you set your structure separation/spacing to be 0/1, you can use
     * isFeatureChunk to return true only if certain chunk coordinates are passed in
     * which allows you to spawn structures only at certain coordinates in the world.
     *
     * Basically, this method is used for determining if the land is at a suitable height,
     * if certain other structures are too close or not, or some other restrictive condition.
     *
     * For example, Pillager Outposts added a check to make sure it cannot spawn within 10 chunk of a Village.
     * (Bedrock Edition seems to not have the same check)
     *
     * If you are doing Nether structures, you'll probably want to spawn your structure on top of ledges.
     * Best way to do that is to use getBaseColumn to grab a column of blocks at the structure's x/z position.
     * Then loop through it and look for land with air above it and set blockpos's Y value to it.
     * Make sure to set the final boolean in JigsawPlacement.addPieces to false so
     * that the structure spawns at blockpos's y value instead of placing the structure on the Bedrock roof!
     *
     * Also, please for the love of god, do not do dimension checking here.
     * If you do and another mod's dimension is trying to spawn your structure,
     * the locate command will make minecraft hang forever and break the game.
     * Use the biome tags for where to spawn the structure and users can datapack
     * it to spawn in specific biomes that aren't in the dimension they don't like if they wish.
     */
        private fun extraSpawningChecks(context: GenerationContext): Boolean {
            // Grabs the chunk position we are at
            val chunkPos: ChunkPos = context.chunkPos()

            // Checks to make sure our structure does not spawn above land that's higher than y = 150
            // to demonstrate how this method is good for checking extra conditions for spawning
            return context.chunkGenerator().getFirstFreeHeight(
                chunkPos.minBlockX,
                chunkPos.minBlockZ,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                context.heightAccessor(),
                context.randomState()
            ) < 150
        }
    }
}
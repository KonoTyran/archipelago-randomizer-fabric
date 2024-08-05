package dev.koifysh.randomizer.data.items

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.server
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.LodestoneTracker
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.Structure
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class StructureCompasses {

    // keep a list of all received compasses
    val compasses: ArrayList<TagKey<Structure>> = ArrayList()



    companion object {
        // refresh all compasses in player inventory
        fun ServerPlayer.refreshCompasses() {
            this.inventory.items.forEach { item ->
                if (item.item == Items.COMPASS) {
                    // return if the compass doesn't have custom data
                    val customData = item.get<CustomData>(DataComponents.CUSTOM_DATA) ?: return@forEach

                    // get the tracked structure from the compass custom data, return if it does not exist.
                    val trackedStructure =
                        customData.unsafe.getString(ArchipelagoRandomizer.modResource("tracked_structure").toString())
                    if (trackedStructure.isBlank()) return@forEach

                    val tagKey =
                        TagKey.create(
                            Registries.STRUCTURE,
                            ResourceLocation.parse(trackedStructure)
                        )
                    item.trackStructure(tagKey, this)
                }
            }
        }


        fun ItemStack.trackStructure(structureTag: TagKey<Structure>, player: Player) {
            val world: ResourceKey<Level> = Utils.getStructureWorld(structureTag)


            var displayName =
                Component.literal(String.format("Structure Compass (%s)", Utils.getAPStructureName(structureTag)))

            // only locate structure if the player is in the same world as the one for the compass
            // otherwise just point it to 0,0 in said dimension.
            var structurePos = BlockPos(0, 0, 0)
            if (player.commandSenderWorld.dimension() == world) {
                try {
                    structurePos =
                        server.getLevel(world)
                            ?.findNearestMapStructure(structureTag, player.blockPosition(), 75, false)!!
                } catch (exception: NullPointerException) {
                    player.sendSystemMessage(
                        Component.literal(
                            "Could not find a nearby " + Utils.getAPStructureName(
                                structureTag
                            )
                        )
                    )
                }
            } else {
                displayName = Component.literal("Structure Compass (${Utils.getAPStructureName(structureTag)}) Wrong Dimension").withStyle(ChatFormatting.DARK_RED)
            }

            //update the nbt data with our new structure.
            this.get(DataComponents.CUSTOM_DATA)!!
                .unsafe.putString(
                    ArchipelagoRandomizer.modResource("tracked_structure").toString(),
                    structureTag.location().toString()
                )


            this.set(
                DataComponents.LODESTONE_TRACKER,
                LodestoneTracker(Optional.of(GlobalPos(world, structurePos)), false)
            )
            this.set(DataComponents.CUSTOM_NAME, displayName)
        }
    }
}
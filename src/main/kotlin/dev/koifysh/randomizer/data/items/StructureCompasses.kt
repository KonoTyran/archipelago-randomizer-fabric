package dev.koifysh.randomizer.data.items

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.compassHandler
import dev.koifysh.randomizer.registries.APItemReward
import dev.koifysh.randomizer.utils.Utils.sendActionBar
import dev.koifysh.randomizer.utils.Utils.setItemLore
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.LodestoneTracker
import net.minecraft.world.level.levelgen.structure.Structure
import java.util.*

class StructureCompasses {
    // keep a list of all received compasses
    val compassNames: HashMap<String, String> = HashMap()
    val compasses: LinkedList<String> = LinkedList()

    fun registerCompass(compass: APItemReward) {
        if (compass !is StructureCompass) return
        if (compass.structure == null || compass.name == null) {
            throw Exception("malformed structure compass entry")
        }
        compassNames[compass.structure] = compass.name
    }

    companion object {
        const val TRACKED_STRUCTURE_STRING = "${ArchipelagoRandomizer.MOD_ID}:tracked_structure"
        const val IS_STATIC_STRING = "${ArchipelagoRandomizer.MOD_ID}:is_static"
        const val NAME_STRING = "${ArchipelagoRandomizer.MOD_ID}:name"

        // refresh all compasses in player inventory
        fun ServerPlayer.refreshCompasses() {
            this.inventory.items.forEach { item ->
                if (item.item != Items.COMPASS) return@forEach
                item.get(DataComponents.CUSTOM_DATA) ?: return@forEach
                item.refreshTrackStructure(this)
            }
        }

        fun ItemStack.cycleToNextStructure(player: ServerPlayer) {
            if (this.item != Items.COMPASS) return
            if (compassHandler.compasses.isEmpty()) {
                refreshTrackStructure(player)
                return
            }
            val customData = this.get(DataComponents.CUSTOM_DATA) ?: return
            val data = customData.unsafe
            if (data.getBoolean(IS_STATIC_STRING)) {
                refreshTrackStructure(player)
                return
            }

            val trackedStructure = data.getString(TRACKED_STRUCTURE_STRING) ?: return

            // get next index
            val nextStructureIndex =
                (compassHandler.compasses.indexOf(trackedStructure) + 1) % compassHandler.compasses.size
            val newStructure = compassHandler.compasses[nextStructureIndex]
            this.trackStructure(newStructure, player)
            data.putString(TRACKED_STRUCTURE_STRING, newStructure)
            data.putString(NAME_STRING, compassHandler.compassNames[newStructure] ?: "Un-named Structure")

        }

        fun ItemStack.refreshTrackStructure(player: ServerPlayer) {
            if (this.item != Items.COMPASS) return
            val customData = this.get(DataComponents.CUSTOM_DATA) ?: return

            this.trackStructure(customData.unsafe.getString(TRACKED_STRUCTURE_STRING), player, customData.unsafe.getBoolean(IS_STATIC_STRING))
        }

        fun ItemStack.trackStructure(structureString: String, player: ServerPlayer) {
            trackStructure(structureString, player, false)
        }

        fun ItemStack.trackStructure(structureString: String, player: ServerPlayer, isStatic: Boolean) {
            if (this.item != Items.COMPASS) return

            val serverLevel = player.serverLevel()
            val globalPos = getStructureCords(structureString, player)
            if (globalPos == null) {
                ArchipelagoRandomizer.logger.warn("Could not find structure: $structureString")
            }

            this.set(
                DataComponents.LODESTONE_TRACKER,
                LodestoneTracker(Optional.ofNullable(globalPos), false)
            )
            player.sendActionBar("Refreshing Compass", 5, 20, 5)
            if(isStatic) {
                val lore: ArrayList<Component> = ArrayList(listOf(Component.literal("Location X: ${globalPos?.pos?.x} Z: ${globalPos?.pos?.z}")))
                if (globalPos?.dimension() != serverLevel.dimension()) lore.clear()
                this.get(DataComponents.LORE)?.let {
                    it.lines.forEach { line ->
                        if (line.string.startsWith("Location X: ")) return@forEach
                        lore.add(line)
                    }
                    this.setItemLore(lore)
                    return
                }

                this.setItemLore(lore)
                return
            }

            var name =  "Structure Compass (${this.get(DataComponents.CUSTOM_DATA)?.unsafe?.getString(NAME_STRING) ?: ""})"
            val lore: ArrayList<String> = arrayListOf("Right click with compass in hand to","cycle though unlocked compasses.")

            var style = Style.EMPTY.withItalic(false)
            if (globalPos?.dimension() != serverLevel.dimension()) {
                name += " Wrong Dimension"
                style = style.withColor(ChatFormatting.DARK_RED)
            } else {
                lore.add(0, "Location X: ${globalPos?.pos?.x} Z: ${globalPos?.pos?.z}")
            }
            val displayName = Component.literal(name).withStyle(style)
            this.set(DataComponents.CUSTOM_NAME, displayName)
            this.setItemLore(lore)

        }

        fun getHolderSet(structure: String): HolderSet<Structure> {
            //make sure registry is present
            val registryOptional = ArchipelagoRandomizer.server.registryAccess().registry(Registries.STRUCTURE)
            if (registryOptional.isEmpty) return HolderSet.empty()

            val structureRegistry = registryOptional.get()

            // if the string starts with a # then it's a tag
            if (structure.startsWith("#")) {
                val prefixRemoved = structure.slice(1 until structure.length)
                val structureTag = TagKey.create(Registries.STRUCTURE, ResourceLocation.parse(prefixRemoved))
                return structureRegistry.getTag(structureTag)?.get() ?: HolderSet.empty()
            } else {
                val holderOptional = structureRegistry.getHolder(ResourceLocation.parse(structure))
                if (holderOptional.isEmpty) return HolderSet.empty()
                val holderSetOptional =
                    holderOptional.map { holder: Holder.Reference<Structure> -> HolderSet.direct(holder) }
                if (holderSetOptional.isEmpty) return HolderSet.empty()
                return holderSetOptional.get()
            }
        }

        fun getStructureCords(structure: String, player: ServerPlayer): GlobalPos? {
            val serverLevel = player.serverLevel()
            val structures = getHolderSet(structure)
            val pair = serverLevel.chunkSource.generator.findNearestMapStructure(
                serverLevel,
                structures,
                player.blockPosition(),
                100,
                false
            )
            if (pair == null) {
                player.server.getLevel(
                    ResourceKey.create(
                        Registries.DIMENSION,
                        ArchipelagoRandomizer.modResource("spawn")
                    )
                )?.let {
                    return GlobalPos(it.dimension(), BlockPos.ZERO)
                }
                return null
            }

            return GlobalPos(serverLevel.dimension(), pair.first)
        }
    }
}
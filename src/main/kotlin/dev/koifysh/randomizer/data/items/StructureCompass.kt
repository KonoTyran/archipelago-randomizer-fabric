package dev.koifysh.randomizer.data.items

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.data.items.StructureCompasses.Companion.refreshTrackStructure
import dev.koifysh.randomizer.registries.APItemReward
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData

data class StructureCompass(
    val structure: String?,
    val name: String?,
    val static: Boolean?,
) : APItemReward() {

    override fun grant(index: Long) {
        if (structure == null || name == null) {
            ArchipelagoRandomizer.logger.warn("malformed structure compass entry")
            return
        }
        val compass = ItemStack(Items.COMPASS)

        val structureData = CustomData.EMPTY
        structureData.unsafe.putString(StructureCompasses.TRACKED_STRUCTURE_STRING, structure)
        structureData.unsafe.putString(StructureCompasses.NAME_STRING, name)
        structureData.unsafe.putBoolean(StructureCompasses.IS_STATIC_STRING, static ?: false)

        compass.set(DataComponents.CUSTOM_DATA, structureData)
        compass.set(DataComponents.CUSTOM_NAME, Component.literal("Structure Compass ($name)"))

        ArchipelagoRandomizer.server.playerList.players.forEach { player ->
            compass.refreshTrackStructure(player)
            Utils.giveItemToPlayer(player, compass)
        }

        if (ArchipelagoRandomizer.compassHandler.compasses.contains(structure)) return
        ArchipelagoRandomizer.compassHandler.compasses.add(structure)
        ArchipelagoRandomizer.compassHandler.compassNames[structure] = name
    }
}

package dev.koifysh.randomizer.base.items

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.base.items.StructureCompasses.Companion.refreshTrackStructure
import dev.koifysh.randomizer.registries.APItemReward
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData

data class StructureCompass(
    val structure: String?,
    val name: String?,
    val static: Boolean?,
) : APItemReward() {

    @Transient
    private lateinit var compass: ItemStack

    private fun init() {
        if (this::compass.isInitialized) return

        compass = ItemStack(Items.COMPASS)
        if (structure == null || name == null) {
            ArchipelagoRandomizer.logger.warn("malformed structure compass entry")

            compass.set(DataComponents.CUSTOM_NAME, Component.literal("Malformed Structure Compass ($name)"))
        } else {
            val structureData = CustomData.EMPTY
            structureData.unsafe.putString(StructureCompasses.TRACKED_STRUCTURE_STRING, structure)
            structureData.unsafe.putString(StructureCompasses.NAME_STRING, name)
            structureData.unsafe.putBoolean(StructureCompasses.IS_STATIC_STRING, static ?: false)
            compass.set(DataComponents.CUSTOM_DATA, structureData)
            compass.set(DataComponents.CUSTOM_NAME, Component.literal("Structure Compass ($name)"))

            ArchipelagoRandomizer.compassHandler.compasses.add(structure)
            ArchipelagoRandomizer.compassHandler.compassNames[structure] = name
        }
    }

    override fun onItemObtain(index: Long) {
        init()

        ArchipelagoRandomizer.server.playerList.players.forEach { player ->
            grantPlayer(player, index)
        }
    }

    override fun grantPlayer(player: ServerPlayer, index: Long) {
        init()

        compass.refreshTrackStructure(player)
        Utils.giveItemToPlayer(player, compass.copy())
    }
}

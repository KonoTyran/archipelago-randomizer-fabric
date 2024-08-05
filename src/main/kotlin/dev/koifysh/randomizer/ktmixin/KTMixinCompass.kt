package dev.koifysh.randomizer.ktmixin

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.commands.Archipelago
import dev.koifysh.randomizer.data.items.StructureCompasses.Companion.refreshCompasses
import dev.koifysh.randomizer.data.items.StructureCompasses.Companion.trackStructure
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import dev.koifysh.randomizer.ArchipelagoRandomizer.compassHandler

object KTMixinCompass {

    fun onPlayerChangeWorld(player: ServerPlayer, origin: ServerLevel, destination: ServerLevel) {
        player.refreshCompasses()
    }

    fun cycleCompass(player: ServerPlayer, level: Level, compass: ItemStack) {
        if (!compass.item.equals(Items.COMPASS)) return


        if (!compass.has(DataComponents.CUSTOM_DATA)) return
        val trackedStructure = compass.get(DataComponents.CUSTOM_DATA)!!.unsafe.getString(ArchipelagoRandomizer.modResource("tracked_structure").toString())
        if (trackedStructure.isBlank()) return

        val location = ResourceLocation.parse(trackedStructure)

        //fetch our current compass list.

        val tagKey = TagKey.create(Registries.STRUCTURE, location)

        if (compassHandler.compasses.isEmpty()) return

        val index = compassHandler.compasses.indexOf(tagKey) + 1 % compassHandler.compasses.size

        val structure = compassHandler.compasses[index]
        compass.trackStructure(structure, player)

    }
}
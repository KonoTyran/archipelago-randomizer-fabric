package dev.koifysh.randomizer.ktmixin

import dev.koifysh.randomizer.rewards.items.StructureCompasses.Companion.cycleToNextStructure
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

object KTMixinCompass {

    fun cycleCompass(player: ServerPlayer, level: Level, compass: ItemStack) {
        if (compass.item != Items.COMPASS) return
        compass.cycleToNextStructure(player)
    }
}
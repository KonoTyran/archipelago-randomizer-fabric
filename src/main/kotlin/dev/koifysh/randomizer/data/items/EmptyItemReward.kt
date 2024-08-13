package dev.koifysh.randomizer.data.items

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APItemReward
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

class EmptyItemReward(unknownType: ResourceLocation): APItemReward() {

    init {
        this.type = unknownType
    }
    override fun onItemObtain(index: Long) {
        ArchipelagoRandomizer.logger.error("Empty item reward of $type.")
    }

    override fun grantPlayer(player: ServerPlayer, index: Long) {
        ArchipelagoRandomizer.logger.error("tried to grant ${player.displayName?.string} empty reward of  $type.")
    }
}
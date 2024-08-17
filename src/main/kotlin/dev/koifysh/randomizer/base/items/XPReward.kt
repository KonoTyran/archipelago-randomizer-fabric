package dev.koifysh.randomizer.base.items

import dev.koifysh.randomizer.registries.APItemReward
import net.minecraft.server.level.ServerPlayer

data class XPReward(
    val amount: Int
) : APItemReward() {

    override fun grantPlayer(player: ServerPlayer, index: Long) {
        player.giveExperiencePoints(amount)
    }
}
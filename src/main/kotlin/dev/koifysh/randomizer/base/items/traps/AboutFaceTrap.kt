package dev.koifysh.randomizer.base.items.traps

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

class AboutFaceTrap : Trap {
    override fun trigger(player: ServerPlayer) {
        player.teleportTo(
            player.level() as ServerLevel,
            player.x,
            player.y,
            player.z,
            player.getYHeadRot() + 180f,
            player.xRot
        )
    }
}
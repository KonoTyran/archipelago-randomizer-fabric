package dev.koifysh.randomizer.traps

import net.minecraft.server.level.ServerPlayer

interface Trap {
    fun trigger(player: ServerPlayer)
}
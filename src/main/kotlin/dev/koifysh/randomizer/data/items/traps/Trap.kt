package dev.koifysh.randomizer.data.items.traps

import net.minecraft.server.level.ServerPlayer

interface Trap {

    fun trigger(player: ServerPlayer)
}
package dev.koifysh.randomizer.base.items.traps

import net.minecraft.server.level.ServerPlayer

interface Trap {

    fun trigger(player: ServerPlayer)
}
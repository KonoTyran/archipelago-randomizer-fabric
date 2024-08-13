package dev.koifysh.randomizer.rewards.items.traps

import net.minecraft.server.level.ServerPlayer

interface Trap {

    fun trigger(player: ServerPlayer)
}
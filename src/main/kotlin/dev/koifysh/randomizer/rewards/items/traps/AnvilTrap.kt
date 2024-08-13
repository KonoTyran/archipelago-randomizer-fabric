package dev.koifysh.randomizer.rewards.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks

class AnvilTrap : Trap {
    override fun trigger(player: ServerPlayer) {
        ArchipelagoRandomizer.server.execute {
            val world: ServerLevel = player.level() as ServerLevel
            val blockPos = BlockPos(player.blockX, player.blockY + 6, player.blockZ)
            if (world.isEmptyBlock(blockPos)) world.setBlock(blockPos, Blocks.DAMAGED_ANVIL.defaultBlockState(), 3)
        }
    }
}
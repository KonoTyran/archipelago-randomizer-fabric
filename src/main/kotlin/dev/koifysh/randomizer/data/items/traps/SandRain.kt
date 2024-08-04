package dev.koifysh.randomizer.data.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3

class SandRain : Trap {
    override fun trigger(player: ServerPlayer) {
        ArchipelagoRandomizer.server.execute {
            val world: ServerLevel = player.level() as ServerLevel
            val pos: Vec3 = player.position()
            val radius = 5
            for (x in pos.x.toInt() - radius..pos.x.toInt() + radius) {
                for (z in pos.z.toInt() - radius..pos.z.toInt() + radius) {
                    val blockPos = BlockPos(x, pos.y.toInt() + 15, z)
                    if (world.isEmptyBlock(blockPos)) world.setBlock(blockPos, Blocks.SAND.defaultBlockState(), 3)
                }
            }
        }
    }
}
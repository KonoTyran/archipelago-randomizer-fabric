package dev.koifysh.randomizer.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import java.util.*

class WaterTrap : Trap {
    private var waterBlocks: MutableList<BlockPos> = LinkedList<BlockPos>()
    private var timer: Int = 20 * 15

    init {
        ServerTickEvents.END_SERVER_TICK.register { onTick() }
    }

    override fun trigger(player: ServerPlayer) {
        val world: ServerLevel = player.level() as ServerLevel
        val pos: Vec3 = player.position()
        val radius = 2
        for (x in pos.x.toInt() - radius..pos.x.toInt() + radius) {
            for (z in pos.z.toInt() - radius..pos.z.toInt() + radius) {
                waterBlocks.add(BlockPos(x, pos.y.toInt() + 3, z))
            }
        }

        ArchipelagoRandomizer.server.execute {
            for (waterBlock in waterBlocks) {
                if (world.isEmptyBlock(waterBlock)) {
                    world.setBlock(waterBlock, Blocks.WATER.defaultBlockState(), 3)
                }
            }
        }
    }

    private fun onTick() {
        if (--timer != 0) return

        for (pos in waterBlocks) {
            val world: ServerLevel = ArchipelagoRandomizer.server.overworld()
            if (world.getBlockState(pos).fluidState.isSourceOfType(Fluids.WATER)) {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
            }
        }
    }
}
package dev.koifysh.randomizer.base.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.phys.Vec3

class CreeperTrap @JvmOverloads constructor(private val numberOfCreepers: Int = 3) : Trap {
    override fun trigger(player: ServerPlayer) {
        ArchipelagoRandomizer.server.execute {
            val world: ServerLevel = player.level() as ServerLevel
            val pos: Vec3 = player.position()
            for (i in 0 until numberOfCreepers) {
                val creeper: Creeper = EntityType.CREEPER.create(world) ?: continue
                creeper.target = player
                creeper.moveTo(Utils.getRandomPosition(pos, 5))
                world.addFreshEntity(creeper)
            }
        }
    }
}
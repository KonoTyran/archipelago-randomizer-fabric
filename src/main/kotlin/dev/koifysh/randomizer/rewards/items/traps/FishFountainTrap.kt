package dev.koifysh.randomizer.rewards.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Silverfish

class FishFountainTrap : Trap {
    override fun trigger(player: ServerPlayer) {
        ArchipelagoRandomizer.server.execute {
            val world: ServerLevel = player.level() as ServerLevel
            for (i in 0..9) {
                val fish: Silverfish = EntityType.SILVERFISH.create(world) ?: continue
                fish.moveTo(Utils.getRandomPosition(player.position(), 5))
                world.addFreshEntity(fish)
            }
        }
    }
}
package dev.koifysh.randomizer.rewards.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.Bee

class BeeTrap @JvmOverloads constructor(private val numberOfBees: Int = 3) : Trap {
    override fun trigger(player: ServerPlayer) {
        ArchipelagoRandomizer.server.execute {
            val world: ServerLevel = player.level() as ServerLevel
            for (i in 0 until numberOfBees) {
                val bee: Bee? = EntityType.BEE.create(world)
                bee?.moveTo(Utils.getRandomPosition(player.position(), 5))
                bee?.persistentAngerTarget = player.uuid
                bee?.remainingPersistentAngerTime = 1200
                if (bee != null) world.addFreshEntity(bee)
            }
        }
    }
}

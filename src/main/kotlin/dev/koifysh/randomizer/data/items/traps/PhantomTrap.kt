package dev.koifysh.randomizer.data.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.Utils
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Phantom
import java.util.*

class PhantomTrap : Trap {
    private var phantoms: MutableList<Phantom> = LinkedList<Phantom>()

    private var timer: Int = 20 * 45

    init {
        ServerTickEvents.END_SERVER_TICK.register { onTick() }
    }

    override fun trigger(player: ServerPlayer) {
        ArchipelagoRandomizer.server.execute {
            val world: ServerLevel = player.level() as ServerLevel
            for (i in 0..2) {
                val phantom: Phantom = EntityType.PHANTOM.create(world) ?: continue
                phantom.addEffect(
                    MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        MobEffectInstance.INFINITE_DURATION,
                        0,
                        false,
                        false
                    )
                )
                phantom.moveTo(Utils.getRandomPosition(player.position(), 5))
                if (world.addFreshEntity(phantom)) phantoms.add(phantom)
            }
        }
    }

    private fun onTick() {
        if (--timer > 0) return

        for (phantom in phantoms) {
            phantom.kill()
        }
    }
}
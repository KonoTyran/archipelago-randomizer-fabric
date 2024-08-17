package dev.koifysh.randomizer.base.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

class BlindnessTrap : Trap {
    override fun trigger(player: ServerPlayer) {
        ArchipelagoRandomizer.server.execute {
            player.addEffect(MobEffectInstance(MobEffects.BLINDNESS, 20 * 10))
            player.addEffect(MobEffectInstance(MobEffects.DARKNESS, 20 * 10))
        }
    }
}
package dev.koifysh.randomizer.base.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

class LevitateTrap : Trap {
    override fun trigger(player: ServerPlayer) {
        ArchipelagoRandomizer.server.execute { player.addEffect(MobEffectInstance(MobEffects.LEVITATION, 20 * 10)) }
    }
}
package dev.koifysh.randomizer.rewards.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

class MiningFatigueTrap : Trap {
    override fun trigger(player: ServerPlayer) {
        ArchipelagoRandomizer.server.execute {
            player.connection.send(ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, 1f))
            player.addEffect(MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20 * 10))
        }
    }
}
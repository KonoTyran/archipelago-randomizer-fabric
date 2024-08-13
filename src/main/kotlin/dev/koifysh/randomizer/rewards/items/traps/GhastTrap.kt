package dev.koifysh.randomizer.rewards.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Ghast

class GhastTrap : Trap {
    override fun trigger(player: ServerPlayer) {
        ArchipelagoRandomizer.server.execute {
            val world: ServerLevel = player.level() as ServerLevel
            val ghast: Ghast = EntityType.GHAST.create(world) ?: return@execute

            ghast.target = player
            ghast.getAttribute(Attributes.MAX_HEALTH)?.baseValue = 15.0
            ghast.addEffect(MobEffectInstance(MobEffects.WITHER, MobEffectInstance.INFINITE_DURATION))
            ghast.moveTo( Utils.getRandomPosition(player.position(), 20))
            world.addFreshEntity(ghast)
        }
    }
}
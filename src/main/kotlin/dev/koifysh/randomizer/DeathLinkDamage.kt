package dev.koifysh.randomizer

import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.LivingEntity

class DeathLinkDamage : DamageSource(damageType) {
    override fun getLocalizedDeathMessage(pLivingEntity: LivingEntity): Component {
        return Component.literal(pLivingEntity.displayName?.string + "'s soul was linked to anothers fate.")
    }

    companion object {
        var DEATH_LINK: ResourceKey<DamageType> = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID, "indirect_magic")
        )
        private val damageType: Holder<DamageType> =
            ArchipelagoRandomizer.server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(
                DEATH_LINK
            )
    }
}

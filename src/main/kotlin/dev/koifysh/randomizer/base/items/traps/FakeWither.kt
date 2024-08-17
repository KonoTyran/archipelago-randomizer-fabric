package dev.koifysh.randomizer.base.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.bossevents.CustomBossEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.BossEvent
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType

class FakeWither : Trap {
    init {
        ServerTickEvents.END_SERVER_TICK.register { onTick() }
    }

    private var done = false

    override fun trigger(player: ServerPlayer) {
        ArchipelagoRandomizer.server.execute {
            witherBar.addPlayer(player)
            witherBar.isVisible = true
            player.addEffect(MobEffectInstance(MobEffects.BLINDNESS, 20 * 6, 0))
            player.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.MASTER, 1f, 1f)
        }
    }

    private fun onTick() {
        if (done) return
        if (!witherBar.isVisible) return
        var value: Int = witherBar.value
        if (value >= witherBar.max) {
            witherBar.value = 0
            witherBar.isVisible = false
            done = true
            return
        }
        witherBar.value = ++value
    }

    companion object {
        private val witherBar: CustomBossEvent = ArchipelagoRandomizer.server.customBossEvents.create(
            ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID, "fake-wither"),
            Component.translatable(EntityType.WITHER.descriptionId)
        )

        init {
            witherBar.color = BossEvent.BossBarColor.PURPLE
            witherBar.setDarkenScreen(true)
            witherBar.max = 300
            witherBar.value = 0
            witherBar.overlay = BossEvent.BossBarOverlay.PROGRESS
            witherBar.isVisible = false
        }
    }
}
package dev.koifysh.randomizer.base.goals

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APGoal
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

class WitherBossGoal : APGoal() {

    init {
        type = ArchipelagoRandomizer.modResource("wither_boss")
        id = type
    }

    override var hasBossBar: Boolean = false
    override var isComplete: Boolean = false

    override fun start() {
        Utils.playSoundToAll(SoundEvents.WITHER_AMBIENT)
        Utils.sendMessageToAll(Component.literal("The Darkness is calling...").withStyle(ChatFormatting.DARK_PURPLE))
        Utils.sendTitleToAll(
            Component.literal("The Darkness").withStyle(ChatFormatting.DARK_PURPLE),
            Component.literal("is calling..."),
            40,
            120,
            40
        )
    }
}
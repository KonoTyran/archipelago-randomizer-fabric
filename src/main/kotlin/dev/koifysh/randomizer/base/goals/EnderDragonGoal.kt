package dev.koifysh.randomizer.base.goals

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APGoal
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

class EnderDragonGoal : APGoal() {

    init {
        type = ArchipelagoRandomizer.modResource("dragon_boss")
        id = type
    }

    override var hasBossBar: Boolean = false
    override var isComplete: Boolean = false

    override fun start() {
        Utils.playSoundToAll(SoundEvents.ENDER_DRAGON_AMBIENT)
        Utils.sendMessageToAll(Component.literal("The Dragon is waiting...").withStyle(ChatFormatting.GOLD))
        Utils.sendTitleToAll(
            Component.literal("The Dragon").withStyle(ChatFormatting.GOLD),
            Component.literal("is waiting..."),
            40,
            120,
            40
        )
    }
}
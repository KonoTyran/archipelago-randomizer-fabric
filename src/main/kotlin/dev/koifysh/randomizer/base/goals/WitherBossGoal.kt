package dev.koifysh.randomizer.base.goals

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APGoal
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.sounds.SoundEvents
import java.awt.Color

class WitherBossGoal : APGoal() {

    init {
        type = ArchipelagoRandomizer.modResource("wither_boss")
    }

    override var hasBossBar: Boolean = false
    override var isComplete: Boolean = false

    override fun start() {
        Utils.playSoundToAll(SoundEvents.WITHER_AMBIENT)
        Utils.sendMessageToAll("The Darkness is calling...")
        Utils.sendTitleToAll(
            Component.literal("The Darkness").withStyle(
                Style.EMPTY.withColor(
                    TextColor.fromRgb(
                        Color.BLACK.rgb
                    )
                )
            ), Component.literal("is calling..."), 40, 120, 40
        )
    }
}
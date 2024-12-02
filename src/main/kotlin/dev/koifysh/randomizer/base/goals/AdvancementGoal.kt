package dev.koifysh.randomizer.base.goals

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APGoal
import net.minecraft.network.chat.Component
import net.minecraft.world.BossEvent

class AdvancementGoal(
    @SerializedName("required_advancements")
    private val requiredAdvancements: Int = 0
) : APGoal() {

    init {
        type = ArchipelagoRandomizer.modResource("advancement")
        id = type
    }

    private val completedAdvancements: Int get() = ArchipelagoRandomizer.archipelagoWorldData.getCompletedLocations().size

    override val isComplete: Boolean get() = completedAdvancements >= requiredAdvancements

    override val hasBossBar = true
    override val bossBarColor = BossEvent.BossBarColor.GREEN
    override val bossBarName: Component get() = Component.literal("Advancements ($completedAdvancements/$requiredAdvancements)")
    override val bossBarMaxValue: Int get() = requiredAdvancements
    override val bossBarCurrentValue: Int get() = completedAdvancements

    override fun start() {
        // No-Op
    }
}
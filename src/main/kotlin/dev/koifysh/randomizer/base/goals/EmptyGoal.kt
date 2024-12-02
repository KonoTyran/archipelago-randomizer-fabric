package dev.koifysh.randomizer.base.goals

import dev.koifysh.randomizer.registries.APGoal
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.BossEvent

class EmptyGoal(unknownType: ResourceLocation) : APGoal() {

    init {
        type = unknownType
    }

    override val isComplete: Boolean = false
    override val hasBossBar: Boolean = true
    override val bossBarColor: BossEvent.BossBarColor = BossEvent.BossBarColor.RED

    override val bossBarCurrentValue: Int = 1
    override val bossBarMaxValue: Int = 1
    override val bossBarName: Component = Component.literal("Invalid goal $type")


    override fun start() {
        // do nothing
    }

}
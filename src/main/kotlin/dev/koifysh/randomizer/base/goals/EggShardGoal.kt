package dev.koifysh.randomizer.base.goals

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APGoal
import net.minecraft.network.chat.Component
import net.minecraft.world.BossEvent.BossBarColor

class EggShardGoal(
    @SerializedName("required_egg_shards")
    var goal: Int = 0,
) : APGoal() {

    init {
        type = ArchipelagoRandomizer.modResource("egg_shards")
        id = type
    }

    private val currentEggShards: Int
        get() = ArchipelagoRandomizer.itemRewardRegister.getReceivedItems()
            .filter { it.rewards.filterIsInstance<EggShardReward>().isNotEmpty() }.size

    override val isComplete: Boolean get() = currentEggShards >= goal

    override val hasBossBar: Boolean = true
    override val bossBarColor: BossBarColor = BossBarColor.PURPLE
    override val bossBarName: Component get() = Component.literal("Egg Shards $currentEggShards/$goal")
    override val bossBarMaxValue: Int get() = goal
    override val bossBarCurrentValue: Int get() = currentEggShards

    override fun start() {
        // no-op
    }
}
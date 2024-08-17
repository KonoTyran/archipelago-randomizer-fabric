package dev.koifysh.randomizer.registries

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.bossevents.CustomBossEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

abstract class APGoal {

    @SerializedName("type")
    var type: ResourceLocation = ResourceLocation.parse("empty:empty")

    @SerializedName("requirements")
    var requirements: ArrayList<ResourceLocation> = ArrayList()

    var hasStarted = false; private set

    /**
     * called when all specified required goals have been completed.
     */
    abstract fun start()

    /**
     * @return true if the goal is completed.
     */
    abstract val isComplete: Boolean

    /**
     * @return true if the goal has a boss bar.
     */
    abstract val hasBossBar: Boolean

    /**
     * @return the color of the boss bar.
     */
    open val bossBarColor: BossBarColor = BossBarColor.WHITE

    /**
     * @return the style of the boss bar.
     */
    open val bossBarOverlay: BossBarOverlay = BossBarOverlay.PROGRESS

    /**
     * @return the name of the boss bar.
     */
    open val bossBarName: Component = Component.empty()

    /**
     * @return the max value of the boss bar.
     */
    open val bossBarMaxValue: Int = 100

    /**
     * @return the current value of the boss bar.
     */
    open val bossBarCurrentValue: Int = 0

    /**
     * call this when you detect this goal is completed.
     */
    fun goalCompleted() {
        if (!hasStarted) return
        updateBossBar()
        ArchipelagoRandomizer.goalRegister.goalCompleted(this)
    }

    internal fun checkRequirementCompletion() {
        if (ArchipelagoRandomizer.goalRegister.getGoalsThatRequire(type).all { it.isComplete }) {
            if (!hasStarted) prepareStart()
            hasStarted = true
        }
    }

    private var bossBar: CustomBossEvent? = null

    internal fun prepareStart() {
        hasStarted = true
        checkCompletion()
        if (hasBossBar) updateBossBar()
        start()
    }

    private fun updateBossBar() {
        if (!hasBossBar) return
        if (isComplete) {
            bossBar?.let {
                it.isVisible = false
                ArchipelagoRandomizer.server.customBossEvents.remove(it)
            }
            return
        }
        bossBar = bossBar ?: ArchipelagoRandomizer.server.customBossEvents.create(type, bossBarName)
        bossBar?.let {
            it.color = bossBarColor
            it.max = bossBarMaxValue
            it.value = bossBarCurrentValue
            it.overlay = bossBarOverlay
            it.name = bossBarName
            ArchipelagoRandomizer.server.playerList.players.forEach { player -> it.addPlayer(player) }
            it.isVisible = !isComplete and hasStarted
        }
    }

    fun checkCompletion() {
        updateBossBar()
        if (isComplete && hasStarted) {
            this.goalCompleted()
        }
    }

    fun addPlayerToBossBar(player: ServerPlayer) {
        bossBar?.addPlayer(player)
    }
}
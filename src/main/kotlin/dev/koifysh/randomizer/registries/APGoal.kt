package dev.koifysh.randomizer.registries

import com.google.gson.annotations.SerializedName
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.BossEvent

abstract class APGoal {

    @SerializedName("id")
    var id: Long = 0

    @SerializedName("type")
    var type: ResourceLocation = ResourceLocation.parse("empty:empty")

    @SerializedName("tracking-advancement")
    var tracker: ResourceLocation = ResourceLocation.parse("empty:empty")

    abstract fun isComplete(): Boolean
    abstract fun hasBossBar(): Boolean
    abstract fun getBossBar(): BossEvent
}
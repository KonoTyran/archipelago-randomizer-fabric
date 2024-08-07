package dev.koifysh.randomizer.registries

import com.google.gson.annotations.SerializedName
import net.minecraft.resources.ResourceLocation

abstract class APItemReward {

    @SerializedName("id")
    var id = 0L

    @SerializedName("type")
    var type: ResourceLocation = ResourceLocation.fromNamespaceAndPath("", "")

    abstract fun grant(index: Long)
}
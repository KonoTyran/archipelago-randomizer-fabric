package dev.koifysh.randomizer.registries

import com.google.gson.annotations.SerializedName
import net.minecraft.resources.ResourceLocation

abstract class APItemReward {

    @SerializedName("type")
    var type: ResourceLocation = ResourceLocation.fromNamespaceAndPath("", "")

    abstract fun grant()
}
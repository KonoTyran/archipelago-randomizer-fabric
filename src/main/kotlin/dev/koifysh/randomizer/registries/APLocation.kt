package dev.koifysh.randomizer.registries

import com.google.gson.annotations.SerializedName
import net.minecraft.resources.ResourceLocation

abstract class APLocation {
    @SerializedName("id") val id: Long = 0
    @SerializedName("type") val type: ResourceLocation = ResourceLocation.parse("empty:empty")
}
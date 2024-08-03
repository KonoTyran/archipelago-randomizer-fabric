package dev.koifysh.randomizer.data.locations

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.registries.APLocation
import net.minecraft.resources.ResourceLocation

class Advancement: APLocation() {
    @SerializedName("advancement") val advancement: ResourceLocation = ResourceLocation.parse("empty:empty")
}
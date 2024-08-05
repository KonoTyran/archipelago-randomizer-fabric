package dev.koifysh.randomizer.data.items

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.registries.APItemReward
import net.minecraft.resources.ResourceLocation

data class TrapItem(
    @SerializedName("trap")
    val trap: ResourceLocation
) : APItemReward() {

    override fun grant(index: Long) {
        TrapItems.trigger(trap)
    }

}
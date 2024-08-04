package dev.koifysh.randomizer.data.items

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.registries.APItem
import dev.koifysh.randomizer.registries.APItemReward
import net.minecraft.resources.ResourceLocation

class TrapItem: APItemReward() {

    @SerializedName("trap_name")
    var trap: ResourceLocation = ResourceLocation.fromNamespaceAndPath("","")

    override fun grant() {
        return TrapItems.trigger(trap)
    }

}
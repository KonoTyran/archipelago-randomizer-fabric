package dev.koifysh.randomizer.data.items

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.registries.APItemReward
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

data class TrapItem(
    @SerializedName("trap")
    val trap: ResourceLocation
) : APItemReward() {

    override fun onItemObtain(index: Long) {
        TrapItems.trigger(trap)
    }

    override fun grantPlayer(player: ServerPlayer, index: Long) {
        //no-op
    }

}
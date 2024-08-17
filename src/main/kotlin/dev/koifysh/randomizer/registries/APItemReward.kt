package dev.koifysh.randomizer.registries

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

abstract class APItemReward {

    @SerializedName("type")
    var type: ResourceLocation = ResourceLocation.fromNamespaceAndPath("", "")

    open fun onItemObtain(index: Long) {
        ArchipelagoRandomizer.server.playerList.players.forEach { grantPlayer(it, index) }
    }

    open fun grantPlayer(player: ServerPlayer, index: Long) {
        // No-Op
    }
}
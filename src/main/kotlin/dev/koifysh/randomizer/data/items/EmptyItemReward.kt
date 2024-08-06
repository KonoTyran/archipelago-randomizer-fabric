package dev.koifysh.randomizer.data.items

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APItemReward
import net.minecraft.resources.ResourceLocation

class EmptyItemReward(unknownType: ResourceLocation): APItemReward() {

    init {
        this.type = unknownType
    }
    override fun grant(index: Long) {
        ArchipelagoRandomizer.logger.error("Empty item reward was granted")
    }
}
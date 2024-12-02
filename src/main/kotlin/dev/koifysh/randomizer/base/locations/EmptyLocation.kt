package dev.koifysh.randomizer.base.locations

import dev.koifysh.randomizer.registries.APLocation
import net.minecraft.resources.ResourceLocation

class EmptyLocation(unknownType: ResourceLocation) : APLocation() {
    init {
        this.type = unknownType
    }
}
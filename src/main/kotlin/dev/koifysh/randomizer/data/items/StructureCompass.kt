package dev.koifysh.randomizer.data.items

import dev.koifysh.randomizer.registries.APItemReward

data class StructureCompass(
    val structure: String
): APItemReward() {

    override fun grant(index: Long) {
        TODO("Not yet implemented")
    }
}

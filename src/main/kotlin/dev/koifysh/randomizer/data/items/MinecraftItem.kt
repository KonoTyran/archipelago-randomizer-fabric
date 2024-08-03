package dev.koifysh.randomizer.data.items

import dev.koifysh.randomizer.registries.APItem

class MinecraftItem: APItem() {

    val item: String = ""
    val amount: Int = 0

    override fun grant() {
        println("Granting Minecraft Item")
    }

}
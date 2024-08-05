package dev.koifysh.randomizer.data.items

import com.mojang.brigadier.StringReader
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.registries.APItemReward
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.commands.arguments.item.ItemInput
import net.minecraft.commands.arguments.item.ItemParser

data class MinecraftItem (
    val item: String,
    val amount: Int
): APItemReward() {

    companion object {
        lateinit var itemParser: ItemParser; internal set
    }

    override fun grant(index: Long) {
        logger.info("Granting item $item")
        val item = itemParser.parse(StringReader(item))
        val itemStack = ItemInput(item.item, item.components).createItemStack(amount, false)
        ArchipelagoRandomizer.server.execute {
            ArchipelagoRandomizer.server.playerList.players.forEach {Utils.giveItemToPlayer(it, itemStack)
            }
        }
    }
}
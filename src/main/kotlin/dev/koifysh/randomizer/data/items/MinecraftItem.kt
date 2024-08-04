package dev.koifysh.randomizer.data.items

import com.mojang.brigadier.StringReader
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.registries.APItemReward
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.commands.arguments.item.ItemInput
import net.minecraft.commands.arguments.item.ItemParser

class MinecraftItem: APItemReward() {

    companion object {
        lateinit var itemParser: ItemParser; internal set
    }

    val item: String = ""
    private val amount: Int = 0

    override fun grant() {
        logger.info("Granting item $item")
        val item = itemParser.parse(StringReader(item))
        val itemStack = ItemInput(item.item, item.components).createItemStack(amount, false)
        ArchipelagoRandomizer.server.execute {
            ArchipelagoRandomizer.server.playerList.players.forEach {Utils.giveItemToPlayer(it, itemStack)
            }
        }
    }
}
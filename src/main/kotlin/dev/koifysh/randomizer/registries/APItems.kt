package dev.koifysh.randomizer.registries

import com.mojang.brigadier.StringReader
import dev.koifysh.archipelago.parts.NetworkItem
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.traps.*
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.commands.arguments.item.ItemInput
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.levelgen.structure.Structure
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.HashMap
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.ArchipelagoRandomizer.server
import dev.koifysh.randomizer.data.APMCData

class APItems {
    private val items = HashMap<Long, ItemStack>()

    private var receivedItems = ArrayList<Long>()
    private val receivedCompasses: ArrayList<TagKey<Structure>> = ArrayList<TagKey<Structure>>()


    private val itemParser = ItemParser(ArchipelagoRandomizer.server.registryAccess())
    fun addItem(itemString: String, count: Int) {
        val item = itemParser.parse(StringReader(itemString))
        val itemStack = ItemInput(item.item, item.components).createItemStack(count, false)
        items[items.size.toLong()] = itemStack
    }

    fun addItem(itemString: String) {
        addItem(itemString, 1)
    }

    fun init(apmcData: APMCData) {
        logger.info("Initializing APItems.")


        val itemParser = ItemParser(ArchipelagoRandomizer.server.registryAccess())
        val compassItem = itemParser.parse(StringReader("minecraft:compass"))
        val compass = ItemInput(compassItem.item, compassItem.components).createItemStack(1, false)
        items[0] = compass

        addItem("minecraft:enchanted_book[minecraft:stored_enchantments={levels:{\"minecraft:depth_strider\":1}}]", 5)
    }

    fun grantItem(itemID: NetworkItem) {
        Utils.sendMessageToAll("Received item: ${itemID.itemName}")
        server.playerList.players.forEach { player ->
            items[1]?.let { Utils.giveItemToPlayer(player, it) }
        }
    }
}

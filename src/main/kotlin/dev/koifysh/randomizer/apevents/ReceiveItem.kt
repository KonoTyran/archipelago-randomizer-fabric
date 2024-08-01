package dev.koifysh.randomizer.apevents

import dev.koifysh.archipelago.Print.APPrintColor
import dev.koifysh.archipelago.events.ArchipelagoEventListener
import dev.koifysh.archipelago.events.ReceiveItemEvent
import dev.koifysh.archipelago.parts.NetworkItem
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor

object ReceiveItem {
    @ArchipelagoEventListener
    fun onReceiveItem(event: ReceiveItemEvent) {
        // Dont fire if we have all ready recevied this location
        if (event.index <= ArchipelagoRandomizer.archipelagoWorldData.getItemIndex()) return

        ArchipelagoRandomizer.archipelagoWorldData.setItemIndex(event.getIndex())

        val item: NetworkItem = event.getItem()
        val textItem: Component = Component.literal(item.itemName)
            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(APPrintColor.gold.color.getRGB())))
        val title: Component = Component.literal("Received")
            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(APPrintColor.red.color.getRGB())))
        Utils.sendTitleToAll(title, textItem, 10, 60, 10)
        ArchipelagoRandomizer.recipeManager.grantRecipe(item.itemID)
        ArchipelagoRandomizer.recipeManager.giveItemToAll(item.itemID)
    }
}

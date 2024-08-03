package dev.koifysh.randomizer.apevents

import dev.koifysh.archipelago.Print.APPrintColor
import dev.koifysh.archipelago.events.ArchipelagoEventListener
import dev.koifysh.archipelago.events.ReceiveItemEvent
import dev.koifysh.archipelago.parts.NetworkItem
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APItems
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor

object ReceiveItem {
    @ArchipelagoEventListener
    fun onReceiveItem(event: ReceiveItemEvent) {
        ArchipelagoRandomizer.server.execute {
            // Dont fire if we have all ready recevied this location
            if (event.index <= ArchipelagoRandomizer.archipelagoWorldData.itemIndex) return@execute

            ArchipelagoRandomizer.archipelagoWorldData.itemIndex = event.index

            val item: NetworkItem = event.item
            val textItem: Component = Component.literal(item.itemName)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(APPrintColor.gold.color.rgb)))
            val title: Component = Component.literal("Received")
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(APPrintColor.red.color.rgb)))
            Utils.sendTitleToAll(title, textItem, 10, 60, 10)
//            APItems.grantItem(item)
        }
    }
}

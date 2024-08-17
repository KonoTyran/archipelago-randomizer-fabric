package dev.koifysh.randomizer.ap.events

import dev.koifysh.archipelago.Print.APPrintJsonType
import dev.koifysh.archipelago.events.ArchipelagoEventListener
import dev.koifysh.archipelago.events.PrintJSONEvent
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.Utils

object PrintJsonListener {

    @ArchipelagoEventListener
    fun onPrintJson(event: PrintJSONEvent) {
        // Don't print chat messages originating from ourselves.
        if (event.type == APPrintJsonType.Chat && event.player != ArchipelagoRandomizer.apClient.slot) return

        Utils.sendFancyMessageToAll(event.apPrint)
    }
}

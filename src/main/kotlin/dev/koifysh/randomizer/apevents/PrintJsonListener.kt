package dev.koifysh.randomizer.apevents

import dev.koifysh.archipelago.events.ArchipelagoEventListener
import dev.koifysh.archipelago.events.PrintJSONEvent
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.Utils

class PrintJsonListener {

    @ArchipelagoEventListener
    fun onPrintJson(event: PrintJSONEvent) {
        // Don't print chat messages originating from ourselves.
        if (event.type == "Chat" && event.player != ArchipelagoRandomizer.apClient.slot) return

        Utils.sendFancyMessageToAll(event.apPrint)
    }
}
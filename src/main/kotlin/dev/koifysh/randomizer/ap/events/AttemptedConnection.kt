package dev.koifysh.randomizer.ap.events

import dev.koifysh.archipelago.events.ArchipelagoEventListener
import dev.koifysh.archipelago.events.ConnectionAttemptEvent
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ap.SlotData
import dev.koifysh.randomizer.utils.Utils

object AttemptedConnection {
    @ArchipelagoEventListener
    fun onAttemptConnect(event: ConnectionAttemptEvent) {
        try {
            val slotData: SlotData = event.getSlotData(SlotData::class.java)
            if (event.seedName != ArchipelagoRandomizer.apmcData.seedName) {
                Utils.sendMessageToAll("Wrong .apmc file found. please stop the server, use the correct .apmc file, delete the world folder, then relaunch the server.")
                event.isCanceled = true
            }
            if (!ArchipelagoRandomizer.validVersions.contains(slotData.client_version)) {
                event.isCanceled = true
                Utils.sendMessageToAll("Game was generated with an for an incompatible version of the Minecraft Randomizer.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

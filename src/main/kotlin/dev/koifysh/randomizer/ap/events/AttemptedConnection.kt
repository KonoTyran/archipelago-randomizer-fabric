package dev.koifysh.randomizer.ap.events

import dev.koifysh.archipelago.events.ArchipelagoEventListener
import dev.koifysh.archipelago.events.ConnectionAttemptEvent
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.data.APMCData
import dev.koifysh.randomizer.data.SlotData
import dev.koifysh.randomizer.utils.Utils

object AttemptedConnection {
    @ArchipelagoEventListener
    fun onAttemptConnect(event: ConnectionAttemptEvent) {
        try {
            val temp: SlotData = event.getSlotData(SlotData::class.java)
            val data: APMCData = ArchipelagoRandomizer.apmcData
            if (event.seedName != data.seedName) {
                Utils.sendMessageToAll("Wrong .apmc file found. please stop the server, use the correct .apmc file, delete the world folder, then relaunch the server.")
                event.isCanceled = true
            }
            if (!ArchipelagoRandomizer.validVersions.contains(temp.client_version)) {
                event.isCanceled = true
                Utils.sendMessageToAll("Game was generated with an for an incompatible version of the Minecraft Randomizer.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

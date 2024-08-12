package dev.koifysh.randomizer.ap.events

import dev.koifysh.archipelago.events.ArchipelagoEventListener
import dev.koifysh.archipelago.events.ConnectionResultEvent
import dev.koifysh.archipelago.helper.DeathLink
import dev.koifysh.archipelago.network.ConnectionResult
import dev.koifysh.randomizer.APClient
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ap.SlotData
import dev.koifysh.randomizer.utils.Utils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class ConnectResult(apClient: APClient) {
    private var client: APClient = apClient

    @ArchipelagoEventListener
    fun onConnectResult(event: ConnectionResultEvent) {
        when (event.result) {
            ConnectionResult.InvalidPassword -> Utils.sendMessageToAll("Connection Failed: Invalid Password.")
            ConnectionResult.IncompatibleVersion -> Utils.sendMessageToAll("Connection Failed: Server Sent Incompatible Version Error.")
            ConnectionResult.InvalidSlot -> Utils.sendMessageToAll("Connection Failed: Invalid Slot Name. (this is case sensitive)")
            ConnectionResult.SlotAlreadyTaken -> Utils.sendMessageToAll("Connection Failed: Room Slot has all ready been taken.")
            ConnectionResult.Success -> {
                Utils.sendMessageToAll("Successfully connected to ${client.connectedAddress}")
                try {
                    client.slotData = event.getSlotData(SlotData::class.java)
                    client.slotData.parseStartingItems()
                } catch (e: Exception) {
                    Utils.sendMessageToAll("Invalid \"starting_items\" section, check logs for more details.")
                    LOGGER.warn("invalid staring items json string: ${client.slotData.startingItems}", e)
                }

                if (client.slotData.MC35) {
                    Utils.sendMessageToAll("Welcome to Minecraft 35.")
                    client.addTag("MC35")
                }
                if (client.slotData.deathlink) {
                    Utils.sendMessageToAll("Welcome to Death Link.")
                    DeathLink.setDeathLinkEnabled(true)
                }
            }
            null -> return
        }
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }
}

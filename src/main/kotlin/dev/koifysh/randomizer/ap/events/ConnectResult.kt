package dev.koifysh.randomizer.ap.events

import dev.koifysh.archipelago.events.ArchipelagoEventListener
import dev.koifysh.archipelago.events.ConnectionResultEvent
import dev.koifysh.archipelago.helper.DeathLink
import dev.koifysh.archipelago.network.ConnectionResult
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.apClient
import dev.koifysh.randomizer.ArchipelagoRandomizer.archipelagoWorldData
import dev.koifysh.randomizer.ap.SlotData
import dev.koifysh.randomizer.utils.Utils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object ConnectResult {

    private val logger: Logger = LogManager.getLogger()

    @ArchipelagoEventListener
    fun onConnectResult(event: ConnectionResultEvent) {
        when (event.result) {
            ConnectionResult.InvalidPassword -> Utils.sendMessageToAll("Connection Failed: Invalid Password.")
            ConnectionResult.IncompatibleVersion -> Utils.sendMessageToAll("Connection Failed: Server Sent Incompatible Version Error.")
            ConnectionResult.InvalidSlot -> Utils.sendMessageToAll("Connection Failed: Invalid Slot Name. (this is case sensitive)")
            ConnectionResult.SlotAlreadyTaken -> Utils.sendMessageToAll("Connection Failed: Room Slot has all ready been taken.")
            ConnectionResult.Success -> {
                Utils.sendMessageToAll("Successfully connected to ${apClient.connectedAddress}")
                try {
                    apClient.slotData = event.getSlotData(SlotData::class.java)
                    apClient.slotData.parseStartingItems()
                } catch (e: Exception) {
                    Utils.sendMessageToAll("Invalid \"starting_items\" section, check logs for more details.")
                    logger.warn("invalid staring items json string: ${apClient.slotData.startingItems}", e)
                }

                if (apClient.slotData.MC35) {
                    Utils.sendMessageToAll("Welcome to Minecraft 35.")
                    apClient.addTag("MC35")
                }
                if (apClient.slotData.deathlink) {
                    Utils.sendMessageToAll("Welcome to Death Link.")
                    DeathLink.setDeathLinkEnabled(true)
                }
                apClient.checkLocations(ArrayList(archipelagoWorldData.getCompletedLocations()))
                ArchipelagoRandomizer.connectionInfoBar.isVisible = !apClient.isConnected
            }
            null -> return
        }
    }
}

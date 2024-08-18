package dev.koifysh.randomizer

import dev.koifysh.archipelago.Client
import dev.koifysh.archipelago.flags.ItemsHandling
import dev.koifysh.randomizer.ArchipelagoRandomizer.apClient
import dev.koifysh.randomizer.ap.events.*
import dev.koifysh.randomizer.utils.Utils
import dev.koifysh.randomizer.ap.SlotData

class APClient: Client() {
    lateinit var slotData: SlotData

    init {
        this.game = "Minecraft"
        this.itemsHandlingFlags = ItemsHandling.SEND_ITEMS or ItemsHandling.SEND_OWN_ITEMS or ItemsHandling.SEND_STARTING_INVENTORY

        eventManager.registerListener(OnDeathLink)
        eventManager.registerListener(OnMC35)
        eventManager.registerListener(ConnectResult)
        eventManager.registerListener(AttemptedConnection)
        eventManager.registerListener(ReceiveItem)
        eventManager.registerListener(LocationChecked)
        eventManager.registerListener(PrintJsonListener)
    }

    override fun onError(ex: Exception) {
        val error = String.format("Connection error: %s", ex.localizedMessage)
        Utils.sendMessageToAll(error)
    }

    override fun onClose(reason: String, attemptingReconnect: Int) {
        if (attemptingReconnect > 0) {
            Utils.sendMessageToAll(String.format("%s \n... reconnecting in %ds", reason, attemptingReconnect))
        } else {
            Utils.sendMessageToAll(reason)
        }
        ArchipelagoRandomizer.connectionInfoBar.isVisible = !apClient.isConnected
    }
}

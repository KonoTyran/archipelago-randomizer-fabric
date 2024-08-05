package dev.koifysh.randomizer

import dev.koifysh.archipelago.flags.ItemsHandling
import dev.koifysh.randomizer.ap.events.*
import dev.koifysh.randomizer.utils.Utils
import dev.koifysh.randomizer.data.SlotData
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class APClient: dev.koifysh.archipelago.Client() {
    lateinit var slotData: SlotData

    init {
        this.game = "Minecraft"
        this.itemsHandlingFlags = ItemsHandling.SEND_ITEMS or ItemsHandling.SEND_OWN_ITEMS or ItemsHandling.SEND_STARTING_INVENTORY


        //give our item manager the list of received items to give to players as they log in.
//        randomizer.itemManager.setReceivedItems(itemManager.receivedItemIDs)

        //reset and catch up our global recipe list to be consistent with what we loaded from our save file.
//        randomizer.recipeManager.resetRecipes()
//        randomizer.recipeManager.grantRecipeList(itemManager.receivedItemIDs)

        eventManager.registerListener(OnDeathLink)
        eventManager.registerListener(OnMC35)
        eventManager.registerListener(ConnectResult(this))
        eventManager.registerListener(AttemptedConnection)
        eventManager.registerListener(ReceiveItem)
        eventManager.registerListener(LocationChecked)
        eventManager.registerListener(PrintJsonListener())
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
//        archipelago.goalManager.updateInfoBar()
    }

    companion object {
        // Directly reference a log4j logger.
        private val LOGGER: Logger = LogManager.getLogger()
    }
}

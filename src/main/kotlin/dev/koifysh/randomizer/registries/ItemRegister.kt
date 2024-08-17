package dev.koifysh.randomizer.registries

import com.google.common.collect.ImmutableList
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.ArchipelagoRandomizer.server
import dev.koifysh.randomizer.registries.deserializers.APItemRewardDeserializer
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import dev.koifysh.randomizer.ArchipelagoRandomizer.archipelagoWorldData as worldData

class ItemRegister {

    private val callbackMethods = HashMap<ResourceLocation, (APItemReward) -> Unit>()
    private val items = HashMap<Long, APItem>()
    var index: Long
        get() = worldData.index
        set(value) {
            worldData.index = value
        }

    fun getReceivedItemIDs(): ImmutableList<Long> {
        return ArchipelagoRandomizer.archipelagoWorldData.getItems()
    }

    fun getReceivedItems(): ImmutableList<APItem> {
        return ImmutableList.copyOf(getReceivedItemIDs().filter { items.containsKey(it)  }.map { items[it]!! })
    }

    fun getItem(id: Long): APItem? = items[id]

    internal fun newItem(item: APItem): Int {
        item.rewards.forEach {
            try {
                callbackMethods[it.type]?.invoke(it)
            } catch (e: Exception) {
                logger.error("Error while invoking method for item ID ${item.id} itemReward type ${it.type}.", e)
                Utils.sendMessageToAll("Error while invoking method for item type ${it.type}. check log for details")
            }
        }

        if (!items.containsKey(item.id)) {
            items[item.id] = item
            return item.rewards.size
        } else {
            logger.warn("Duplicate item id ${item.id}. Skipping.")
        }
        return 0
    }

    internal fun sendItem(id: Long, index: Long) {
        this.index = index
        worldData.addItem(id)
        server.playerList.players.forEach { worldData.updatePlayerIndex(it.stringUUID, index.toInt()) }
        items[id]?.rewards?.forEach {
            try {
                it.onItemObtain(index)
            } catch (e: Exception) {
                logger.error("Error while granting item type ${it.type}.", e)
                Utils.sendMessageToAll("Error while granting item type ${it.type}. check log for details")
            }
        }
    }

    fun <T : APItemReward> register(type: ResourceLocation, location: Class<T>, consumer: ((APItemReward) -> Unit)?) {
        if (!APItemRewardDeserializer.register(type, location))
            logger.warn("attempted to register duplicate Item Reward type $type, skipping")
        if (consumer != null)
            callbackMethods[type] = consumer
    }

    fun <T : APItemReward> register(type: ResourceLocation, location: Class<T>) {
        register(type, location, null)
    }

    fun catchUpPlayer(player: ServerPlayer) {
        val worldData = ArchipelagoRandomizer.archipelagoWorldData
        ArchipelagoRandomizer.itemRewardRegister.getReceivedItems().forEachIndexed { i, item ->
            val index = i + 1
            if(worldData.getPlayerIndex(player.stringUUID) >= index) return
            worldData.updatePlayerIndex(player.stringUUID, index)
            item.rewards.forEach { reward ->
                reward.grantPlayer(player, index.toLong())
            }
        }
    }

    fun clear() {
        items.clear()
    }

    fun isEmpty(): Boolean {
        return items.isEmpty()
    }
}

package dev.koifysh.randomizer.registries

import com.google.common.collect.ImmutableList
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.registries.deserializers.APItemRewardDeserializer
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.resources.ResourceLocation
import dev.koifysh.randomizer.ArchipelagoRandomizer.archipelagoWorldData as worldData

class ItemRegister {

    private val locationMethods = HashMap<ResourceLocation, (APItemReward) -> Unit>()
    private val items = HashMap<Long, APItem>()
    var index: Long = 0
        get() = worldData.itemIndex
        set(value) {
            field = value
            worldData.itemIndex = value
        }

    fun getReceivedItemIDs(): List<Long> = ImmutableList.copyOf(ArchipelagoRandomizer.apClient.itemManager.receivedItems.map { it.itemID })

    fun getReceivedItems(): List<APItem> {
        return getReceivedItemIDs().filter { items.containsKey(it)  }.map { items[it]!! }
    }

    fun getItem(id: Long): APItem? = items[id]

    internal fun newItem(item: APItem): Int {
        item.rewards.forEach {
            try {
                locationMethods[it.type]?.invoke(it)
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
        items[id]?.rewards?.forEach {
            try {
                it.grant(index)
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
            locationMethods[type] = consumer
    }

    fun <T : APItemReward> register(type: ResourceLocation, location: Class<T>) {
        register(type, location, null)
    }

    fun clear() {
        items.clear()
    }

    fun isEmpty(): Boolean {
        return items.isEmpty()
    }
}

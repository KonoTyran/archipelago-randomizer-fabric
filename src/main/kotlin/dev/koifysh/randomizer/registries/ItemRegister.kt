package dev.koifysh.randomizer.registries

import com.google.common.collect.ImmutableList
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.registries.deserializers.APItemRewardDeserializer
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.resources.ResourceLocation
import dev.koifysh.randomizer.ArchipelagoRandomizer.archipelagoWorldData as worldData

class ItemRegister {

    private val items = HashMap<Long, ArrayList<APItemReward>>()
    private var receivedItems = ArrayList<Long>()
    var index: Long = 0
    get() = worldData.itemIndex
    set(value) {
            field = value
            worldData.itemIndex = value
        }

    fun getReceivedItems(): List<Long> = ImmutableList.copyOf(receivedItems)


    internal fun newItem(item: APItem): Int {
        var unknown = 0
        item.rewards.forEach {
            if (!APItemRewardDeserializer.isKnown(it.type)) {
                logger.warn("Unknown item type ${it.type}.")
                unknown++
            }
        }

        if (!items.containsKey(item.id)) {
            items[item.id] = item.rewards
            return item.rewards.size - unknown
        } else {
            logger.warn("Duplicate item id ${item.id}. Skipping.")
        }
        return 0
    }

    internal fun sendItem(id: Long, index: Long) {
        this.index = index
        receivedItems.add(id)
        items[id]?.forEach {
            try {
                it.grant()
            } catch (e: Exception) {
                logger.error("Error while granting item type ${it.type}. ${e.message}")
                Utils.sendMessageToAll("Error while granting item type ${it.type}. check log for details")
            }
        }
    }

    fun <T : APItemReward> register(type: ResourceLocation, location: Class<T>) {
        if (!APItemRewardDeserializer.register(type, location))
            logger.warn("attempted to register duplicate Item Reward type $type, skipping")
    }

    fun clear() {
        items.clear()
    }
}
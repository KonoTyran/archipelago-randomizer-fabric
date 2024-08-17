package dev.koifysh.randomizer.registries.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.koifysh.randomizer.ArchipelagoRandomizer.gson
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.base.items.EmptyItemReward
import dev.koifysh.randomizer.registries.APItemReward
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type

object APItemRewardDeserializer : JsonDeserializer<APItemReward> {
    private const val TYPE_STRING = "type"
    private val itemRegistry: HashMap<ResourceLocation, Class<out APItemReward>> = HashMap()

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): APItemReward {
        val itemObject: JsonObject = json.asJsonObject
        if (!itemObject.has(TYPE_STRING)) {
            logger.error("Item type not specified. Skipping.")
            return EmptyItemReward(ResourceLocation.fromNamespaceAndPath("Type", "NotSpecified"))
        }
        val itemTypeElement: JsonElement = itemObject[TYPE_STRING]
        val itemTypeLocation = ResourceLocation.parse(itemTypeElement.asString)

        if (!itemRegistry.containsKey(itemTypeLocation)) {
            logger.error("Unknown item type: $itemTypeLocation")
            return EmptyItemReward(itemTypeLocation)
        }

        val itemType: Class<out APItemReward> = itemRegistry[itemTypeLocation] as Class<out APItemReward>
        try {
            return gson.fromJson(itemObject, itemType)
        } catch (e: Exception) {
            logger.error("Error while deserializing item type $itemTypeLocation. json: $itemObject \n ${e.message}")
            return EmptyItemReward(itemTypeLocation)
        }
    }

    internal fun <T : APItemReward> register(type: ResourceLocation, item: Class<T>): Boolean {
        if (itemRegistry.containsKey(type)) return false
        itemRegistry[type] = item
        return true
    }

}
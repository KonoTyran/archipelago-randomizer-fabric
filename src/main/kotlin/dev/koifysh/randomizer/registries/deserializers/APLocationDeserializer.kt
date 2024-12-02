package dev.koifysh.randomizer.registries.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.koifysh.randomizer.ArchipelagoRandomizer.gson
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.base.locations.EmptyLocation
import dev.koifysh.randomizer.registries.APLocation
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type

object APLocationDeserializer : JsonDeserializer<APLocation> {
    private const val TYPE_STRING = "type"
    private val locationRegistry: HashMap<ResourceLocation, Class<out APLocation>> = HashMap()

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): APLocation {
        val locationObject: JsonObject = json.asJsonObject
        if (!locationObject.has(TYPE_STRING)) return EmptyLocation(
            ResourceLocation.fromNamespaceAndPath(
                "Type",
                "NotSpecified"
            )
        )
        val locationTypeElement: JsonElement = locationObject[TYPE_STRING]

        val locationTypeLocation = ResourceLocation.parse(locationTypeElement.asString)

        if (!locationRegistry.containsKey(locationTypeLocation)) {
            logger.error("Unknown location type: $locationTypeLocation")
            return EmptyLocation(locationTypeLocation)
        }

        try {
            return gson.fromJson(
                locationObject,
                locationRegistry[ResourceLocation.parse(locationTypeElement.asString)] as Class<out APLocation>
            )
        } catch (e: Exception) {
            logger.error("Error while deserializing location type $locationTypeLocation. json: $locationObject \n ${e.message}")
            return EmptyLocation(locationTypeLocation)
        }
    }

    internal fun <T : APLocation> register(type: ResourceLocation, location: Class<T>): Boolean {
        if (locationRegistry.containsKey(type)) return false
        locationRegistry[type] = location
        return true
    }


}
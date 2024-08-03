package dev.koifysh.randomizer.registries.deserializers

import com.google.gson.*
import dev.koifysh.randomizer.registries.APLocation
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type
import dev.koifysh.randomizer.ArchipelagoRandomizer.gson

object APLocationDeserializer: JsonDeserializer<APLocation> {
    private val TYPE_STRING = "type"
    private val locationRegistry: HashMap<ResourceLocation, Class<out APLocation>> = HashMap()

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): APLocation {
        val locationObject: JsonObject = json.asJsonObject
        val locationTypeElement: JsonElement = locationObject[TYPE_STRING]

        val locationType: Class<out APLocation> = locationRegistry[ResourceLocation.parse(locationTypeElement.asString)] as Class<out APLocation>
        return gson.fromJson(locationObject, locationType)
    }

    internal fun <T: APLocation> register(type: ResourceLocation, location: Class<T>) {
        if (locationRegistry.containsKey(type)) throw IllegalArgumentException("Location $type already registered.")
        locationRegistry[type] = location
    }


}
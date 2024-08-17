package dev.koifysh.randomizer.registries.deserializers

import com.google.gson.*
import dev.koifysh.randomizer.registries.APGoal
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type
import dev.koifysh.randomizer.ArchipelagoRandomizer.gson

object APGoalDeserializer: JsonDeserializer<APGoal> {
    private const val TYPE_STRING = "type"
    private val goalRegistry: HashMap<ResourceLocation, Class<out APGoal>> = HashMap()

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): APGoal {
        val goalObject: JsonObject = json.asJsonObject
        if(!goalObject.has(TYPE_STRING)) throw JsonParseException("APGoal is missing \"$TYPE_STRING\" field $goalObject")
        val goalTypeElement: JsonElement = goalObject[TYPE_STRING]

        val goalType: Class<out APGoal> = goalRegistry[ResourceLocation.parse(goalTypeElement.asString)] as Class<out APGoal>
        return gson.fromJson(goalObject, goalType)
    }

    internal fun <T: APGoal> register(type: ResourceLocation, location: Class<T>) : Boolean {
        if (goalRegistry.containsKey(type)) return false
        goalRegistry[type] = location
        return true
    }
}
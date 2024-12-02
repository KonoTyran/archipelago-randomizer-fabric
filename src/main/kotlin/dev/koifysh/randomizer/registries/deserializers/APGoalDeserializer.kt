package dev.koifysh.randomizer.registries.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.koifysh.randomizer.ArchipelagoRandomizer.gson
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.base.goals.EmptyGoal
import dev.koifysh.randomizer.registries.APGoal
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type

object APGoalDeserializer : JsonDeserializer<APGoal> {
    private const val TYPE_STRING = "type"
    private val goalRegistry: HashMap<ResourceLocation, Class<out APGoal>> = HashMap()
    private val idList: HashSet<ResourceLocation> = HashSet()

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): APGoal {
        val goalObject: JsonObject = json.asJsonObject
        if (!goalObject.has(TYPE_STRING)) return EmptyGoal(
            ResourceLocation.fromNamespaceAndPath(
                "type",
                "not_specified"
            )
        )
        if (!goalObject.has("id")) {
            logger.error("Goal does not have an ID. json: $goalObject")
            return EmptyGoal(
                ResourceLocation.fromNamespaceAndPath(
                    "id",
                    "not_specified"
                )
            )
        }

        val goalTypeLocation = ResourceLocation.parse(goalObject[TYPE_STRING].asString)
        val goalID = ResourceLocation.parse(goalObject["id"].asString)

        if (idList.contains(goalID)) {
            logger.error("Duplicate goal ID: ${goalObject["id"]}")
            return EmptyGoal(
                ResourceLocation.fromNamespaceAndPath(
                    "id",
                    "duplicate"
                )
            )
        }

        if (!goalRegistry.containsKey(goalTypeLocation)) {
            logger.error("Unknown goal type: $goalTypeLocation")
            return EmptyGoal(goalTypeLocation)
        }

        try {
            return gson.fromJson(goalObject, goalRegistry[goalTypeLocation] as Class<out APGoal>)
        } catch (e: Exception) {
            logger.error("Error while deserializing goal type $goalTypeLocation. json: $goalObject \n ${e.message}")
            return EmptyGoal(goalTypeLocation)
        }
    }

    internal fun <T : APGoal> register(type: ResourceLocation, location: Class<T>): Boolean {
        if (goalRegistry.containsKey(type)) return false
        goalRegistry[type] = location
        return true
    }
}
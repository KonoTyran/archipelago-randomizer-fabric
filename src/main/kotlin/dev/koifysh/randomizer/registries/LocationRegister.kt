package dev.koifysh.randomizer.registries

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.locationRegister
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.registries.deserializers.APLocationDeserializer
import net.minecraft.resources.ResourceLocation
import java.util.LinkedList

class LocationRegister {

    private val locationMethods = HashMap<ResourceLocation, (APLocation) -> Unit>()
    private val apLocations = HashMap<Long, APLocation>()

    fun <T: APLocation> register(type: ResourceLocation, location: Class<T>, consumer: (APLocation) -> Unit) {
        if(APLocationDeserializer.register(type, location))
            locationMethods[type] = consumer
        else
            logger.error("attempted to register duplicate location type $type, skipping")
    }

    fun sendLocation(id : Long) {
        ArchipelagoRandomizer.apClient.locationManager.checkLocation(id)
    }

    internal fun newLocation(apLocation: APLocation): Int {
        try {
            locationMethods[apLocation.type]?.invoke(apLocation)
            apLocations[apLocation.id] = apLocation
            return 1
        } catch (e: Exception) {
            logger.error("Error while processing location ${apLocation.id} of type ${apLocation.type}. ${e.message}")
        }
        return 0
    }

    fun isEmpty(): Boolean {
        return apLocations.isEmpty()
    }

}



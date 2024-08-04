package dev.koifysh.randomizer.registries

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.registries.deserializers.APLocationDeserializer
import net.minecraft.resources.ResourceLocation

class LocationRegister {

    private val locationMethods = HashMap<ResourceLocation, (APLocation) -> Unit>()
    private val sentLocations = HashSet<Long>()

    fun <T: APLocation> register(type: ResourceLocation, location: Class<T>, consumer: (APLocation) -> Unit) {
        if(APLocationDeserializer.register(type, location))
            locationMethods[type] = consumer
        else
            logger.error("attempted to register duplicate location type $type, skipping")
    }

    fun sendLocation(id : Long) {
        sentLocations.add(id)
        ArchipelagoRandomizer.apClient.locationManager.checkLocation(id)
    }

    internal fun newLocation(apLocation: APLocation): Int {
        try {
            locationMethods[apLocation.type]?.invoke(apLocation)
            return 1
        } catch (e: Exception) {
            logger.error("Error while processing location ${apLocation.id} of type ${apLocation.type}. ${e.message}")
        }
        return 0
    }

}



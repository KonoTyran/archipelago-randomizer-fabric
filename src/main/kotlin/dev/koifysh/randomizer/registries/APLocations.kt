package dev.koifysh.randomizer.registries

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.deserializers.APLocationDeserializer
import net.minecraft.resources.ResourceLocation

class APLocations {

    private val locationMethods = HashMap<ResourceLocation, (APLocation) -> Unit>()
    private val sentLocations = HashSet<Long>()

    fun <T: APLocation> register(type: ResourceLocation, location: Class<T>, consumer: (APLocation) -> Unit) {
        APLocationDeserializer.register(type, location)
        locationMethods[type] = consumer
    }

    fun sendLocation(id : Long) {
        sentLocations.add(id)
        ArchipelagoRandomizer.apClient.locationManager.checkLocation(id)
    }

    internal fun newLocation(apLocation: APLocation) {
        try {
            locationMethods[apLocation.type]?.invoke(apLocation)
        } catch (e: Exception) {
            ArchipelagoRandomizer.logger.error("Error while processing location ${apLocation.id} of type ${apLocation.type}. ${e.message}")
        }
    }

}



package dev.koifysh.randomizer.data

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.apmcData
import dev.koifysh.randomizer.ArchipelagoRandomizer.gson
import dev.koifysh.randomizer.ArchipelagoRandomizer.itemRegister
import dev.koifysh.randomizer.ArchipelagoRandomizer.locationRegister
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.registries.APItem
import dev.koifysh.randomizer.registries.APLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.packs.resources.ResourceManager

object DefaultDataLoader {

    fun loadDefaultData(server: MinecraftServer, resourceManager: ResourceManager) {
        if (apmcData.apLocations.isEmpty()) {
            logger.info("Loading default locations")
            resourceManager.getResource(ArchipelagoRandomizer.modResource("default_data/default_locations.json"))
                .ifPresent { resource ->
                    try {
                        val locations = gson.fromJson(resource.openAsReader(), Array<APLocation>::class.java)
                        var locationCount = 0
                        locations.forEach {
                            locationCount += locationRegister.newLocation(it)
                        }
                        logger.info("$locationCount default locations loaded.")
                    } catch (e: Exception) {
                        logger.error("Failed to load default locations", e)
                    }
                }
        }

        if (apmcData.apItems.isEmpty()) {
            itemRegister.clear()
            logger.info("Loading locations items")
            resourceManager.getResource(ArchipelagoRandomizer.modResource("default_data/default_items.json"))
                .ifPresent { resource ->
                    val items = gson.fromJson(resource.openAsReader(), Array<APItem>::class.java)
                    var rewardsCount = 0
                    var itemsCount = 0
                    items.forEach {
                        val localCount = itemRegister.newItem(it)
                        if (localCount == 0) {
                            logger.warn("No items loaded for item ${it.id} is \"rewards\" field empty?")
                        }
                        rewardsCount += localCount
                        itemsCount++
                    }
                    logger.info("$itemsCount default items and $rewardsCount rewards loaded.")
                }
        }
    }
}
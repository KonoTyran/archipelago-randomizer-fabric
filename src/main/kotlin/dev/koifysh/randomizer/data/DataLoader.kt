package dev.koifysh.randomizer.data

import com.google.gson.reflect.TypeToken
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.apmcData
import dev.koifysh.randomizer.ArchipelagoRandomizer.gson
import dev.koifysh.randomizer.ArchipelagoRandomizer.itemRewardRegister
import dev.koifysh.randomizer.ArchipelagoRandomizer.locationRegister
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.registries.APGoal
import dev.koifysh.randomizer.registries.APItem
import dev.koifysh.randomizer.registries.APLocation
import dev.koifysh.randomizer.utils.ZipUtils.decompressToString
import net.minecraft.server.packs.resources.ResourceManager
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object DataLoader {

    fun loadAPMCData(): APMCData {
        logger.info("Loading APMC file")
        try {
            val path = Paths.get("./APData/")
            if (!Files.exists(path)) {
                Files.createDirectories(path)
                logger.info("APData folder missing, creating.")
            }

            val files = checkNotNull(File(path.toUri()).listFiles { _: File, name: String -> name.endsWith(".apmc") })
            Arrays.sort(files, Comparator.comparingLong { obj: File -> obj.lastModified() })
            try {
                val rawString = Files.readString(files[0].toPath())
                if (rawString.startsWith("{")) { // raw json string
                    return gson.fromJson(rawString, APMCData::class.java)
                } else if (rawString.startsWith("e")) {// 64encoded json string
                    return gson.fromJson(String(Base64.getDecoder().decode(rawString)), APMCData::class.java)
                }
            } catch (ignored: IOException) {
                // most likely PKZipped byte array format
                val zipped = Files.readAllBytes(files[0].toPath())
                val json = zipped.decompressToString()
                return gson.fromJson(json, APMCData::class.java)
            }
        } catch (e: Exception) {
            logger.error("Error Loading APMC File: ${e.cause.toString()} ${e.message}")
        }
        val badAPMC = APMCData()

        badAPMC.state = APMCData.State.MISSING
        return badAPMC
    }

    internal fun loadLocations(locations: Collection<APLocation>) {
        var locationCount = 0
        locations.forEach {
            locationCount += locationRegister.newLocation(it)
        }
        if (locationCount > 0)
            logger.info("$locationCount locations loaded.")
    }

    internal fun loadItems(items: Collection<APItem>) {
        var rewardsCount = 0
        var itemsCount = 0
        items.forEach {
            val localCount = itemRewardRegister.newItem(it)
            rewardsCount += localCount
            itemsCount++
        }
        if (itemsCount > 0 || rewardsCount > 0)
            logger.info("$itemsCount items and $rewardsCount rewards loaded.")
    }

    internal fun loadGoals(goals: Collection<APGoal>): Int {
        var goalCount = 0
        goals.forEach {
            goalCount += ArchipelagoRandomizer.goalRegister.newGoal(it)
        }
        if (goalCount > 0)
            logger.info("$goalCount goals loaded.")
        return goalCount
    }

    fun loadDefaultData(resourceManager: ResourceManager) {
        if (apmcData.apLocations.isEmpty() && locationRegister.isEmpty()) {
            logger.info("Loading default locations")
            resourceManager.getResource(ArchipelagoRandomizer.modResource("default_data/default_locations.json"))
                .ifPresent {
                    loadLocations(
                        gson.fromJson(
                            it.openAsReader(),
                            object : TypeToken<List<APLocation>>() {}.type
                        )
                    )
                }
        }

        if (apmcData.apItems.isEmpty() && itemRewardRegister.isEmpty()) {
            logger.info("Loading default items")
            resourceManager.getResource(ArchipelagoRandomizer.modResource("default_data/default_items.json"))
                .ifPresent { loadItems(gson.fromJson(it.openAsReader(), object : TypeToken<List<APItem>>() {}.type)) }
        }
    }
}

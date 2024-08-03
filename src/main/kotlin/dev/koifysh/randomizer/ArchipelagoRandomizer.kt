package dev.koifysh.randomizer

import com.google.common.collect.ImmutableSet
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.koifysh.randomizer.data.APMCData
import dev.koifysh.randomizer.data.ArchipelagoWorldData
import dev.koifysh.randomizer.data.locations.Advancement
import dev.koifysh.randomizer.data.locations.AdvancementLocation
import dev.koifysh.randomizer.registries.APGoals
import dev.koifysh.randomizer.registries.APLocation
import dev.koifysh.randomizer.registries.APLocations
import dev.koifysh.randomizer.registries.deserializers.APLocationDeserializer
import dev.koifysh.randomizer.structure.ArchipelagoStructures
import dev.koifysh.randomizer.traps.Traps
import dev.koifysh.randomizer.utils.TitleQueue
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object ArchipelagoRandomizer : ModInitializer {

    lateinit var gson: Gson; private set
    const val MOD_ID = "archipelago-randomizer"
    private const val MOD_VERSION = "Archipelago Randomizer v0.1.3"

    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    var apmcData = APMCData(); private set
    lateinit var apClient: APClient private set
    lateinit var server: MinecraftServer private set
    lateinit var archipelagoWorldData: ArchipelagoWorldData private set

    lateinit var locationRegister: APLocations private set

    val validVersions: ImmutableSet<Int> = ImmutableSet.of(
        9, // mc 1.19
        10 // mc 1.21
    )

    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        logger.info("$MOD_VERSION initializing.")

        locationRegister = APLocations()
        val advancementLocation = AdvancementLocation()
        locationRegister.register(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "advancement"),
            Advancement::class.java,
            advancementLocation::addLocation
        )

        // load apmc file
        val builder = GsonBuilder()
        builder.registerTypeAdapter(APLocation::class.java, APLocationDeserializer)
        builder.registerTypeAdapter(ResourceLocation::class.java, ResourceLocation.Serializer())
        gson = builder.create()

        ServerLifecycleEvents.SERVER_STARTING.register(this::beforeLevelLoad)
        ServerLifecycleEvents.SERVER_STARTED.register(this::afterLevelLoad)
        ServerTickEvents.END_SERVER_TICK.register(TitleQueue::onServerTick)

        ArchipelagoStructures.registerStructures()
    }

    private fun beforeLevelLoad(minecraftServer: MinecraftServer) {
        server = minecraftServer
        logger.info("$MOD_VERSION starting.")
    }


    private fun afterLevelLoad(minecraftServer: MinecraftServer) {
        logger.info("$MOD_VERSION started.")
        archipelagoWorldData = server.overworld().dataStorage.computeIfAbsent(ArchipelagoWorldData.factory(), MOD_ID)

        apClient = APClient()
        apClient.setName(apmcData.playerName)
        apClient.connect("${apmcData.server}:${apmcData.port}")

        APGoals.init(apmcData)
        Traps.init()
    }

    internal fun modResource(location: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, location)
    }

    fun loadAPMCData() {
        logger.info("Loading APMCData file")
        try {
            val path = Paths.get("./APData/")
            if (!Files.exists(path)) {
                Files.createDirectories(path)
                logger.info("APData folder missing, creating.")
            }

            val files = checkNotNull(File(path.toUri()).listFiles { _: File, name: String -> name.endsWith(".apmc") })
            Arrays.sort(files, Comparator.comparingLong { obj: File -> obj.lastModified() })
            val json = Files.readString(files[0].toPath())
//			val json = String(Base64.getDecoder().decode(b64))
            apmcData = gson.fromJson(json, APMCData::class.java)
            if (!validVersions.contains(apmcData.clientVersion)) {
                apmcData.state = APMCData.State.INVALID_VERSION
            }

        } catch (e: IOException) {
            apmcData.state = APMCData.State.MISSING
        } catch (e: NullPointerException) {
            apmcData.state = APMCData.State.MISSING
        } catch (e: ArrayIndexOutOfBoundsException) {
            apmcData.state = APMCData.State.MISSING
        } catch (e: AssertionError) {
            apmcData.state = APMCData.State.MISSING
        }

        if (apmcData.state == APMCData.State.MISSING) {
            logger.error("no .apmc file found. please place .apmc file in './APData/' folder. ")
        }

        apmcData.apLocations.forEach(locationRegister::newLocation)
    }
}
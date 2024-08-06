package dev.koifysh.randomizer

import com.google.common.collect.ImmutableSet
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.koifysh.randomizer.commands.Archipelago
import dev.koifysh.randomizer.commands.Connect
import dev.koifysh.randomizer.commands.Disconnect
import dev.koifysh.randomizer.commands.Start
import dev.koifysh.randomizer.data.APMCData
import dev.koifysh.randomizer.data.ArchipelagoWorldData
import dev.koifysh.randomizer.data.DefaultDataLoader
import dev.koifysh.randomizer.data.items.*
import dev.koifysh.randomizer.data.locations.Advancement
import dev.koifysh.randomizer.data.locations.AdvancementLocations
import dev.koifysh.randomizer.events.player.PlayerEvents
import dev.koifysh.randomizer.registries.*
import dev.koifysh.randomizer.registries.deserializers.APItemRewardDeserializer
import dev.koifysh.randomizer.registries.deserializers.APLocationDeserializer
import dev.koifysh.randomizer.structure.ArchipelagoStructures
import dev.koifysh.randomizer.utils.TitleQueue
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.packs.PackType
import net.minecraft.util.RandomSource
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
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

    lateinit var locationRegister: LocationRegister private set
    lateinit var itemRegister: ItemRegister private set

    lateinit var itemHandler: MinecraftItems private set
    lateinit var compassHandler: StructureCompasses private set

    private var jailCenter: BlockPos = BlockPos.ZERO

    val advancementLocations = AdvancementLocations()

    val validVersions: ImmutableSet<Int> = ImmutableSet.of(
        9, // mc 1.19
        10 // mc 1.21
    )

    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        logger.info("$MOD_VERSION initializing.")

        locationRegister = LocationRegister()
        itemRegister = ItemRegister()

        compassHandler = StructureCompasses()

        locationRegister.register(
            modResource( "advancement"),
            Advancement::class.java,
            advancementLocations::addLocation
        )

        itemRegister.register(
            modResource( "item"),
            MinecraftItem::class.java
        )

        itemRegister.register(
            modResource("trap"),
            TrapItem::class.java
        )

        itemRegister.register(
            modResource("structure_compass"),
            StructureCompass::class.java,
            compassHandler::registerCompass
        )

        // load apmc file
        val builder = GsonBuilder()
        builder.registerTypeAdapter(ResourceLocation::class.java, ResourceLocation.Serializer())
        builder.registerTypeAdapter(APLocation::class.java, APLocationDeserializer)
        builder.registerTypeAdapter(APItemReward::class.java, APItemRewardDeserializer)
        gson = builder.create()


        // Register Events
        ServerLifecycleEvents.SERVER_STARTING.register(this::beforeLevelLoad)
        ServerLifecycleEvents.SERVER_STARTED.register(this::afterLevelLoad)
        ServerTickEvents.END_SERVER_TICK.register(TitleQueue::onServerTick)
        ServerPlayConnectionEvents.JOIN.register(PlayerEvents::onPlayerJoin)
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(PlayerEvents::onPlayerChangeWorld)


        // Register Commands
        CommandRegistrationCallback.EVENT.register(Connect::register)
        CommandRegistrationCallback.EVENT.register(Start::register)
        CommandRegistrationCallback.EVENT.register(Archipelago::register)
        CommandRegistrationCallback.EVENT.register(Disconnect::register)


        // Load Structures
        ArchipelagoStructures.registerStructures()

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(DefaultDataLoader())
    }

    private fun beforeLevelLoad(minecraftServer: MinecraftServer) {
        server = minecraftServer
        logger.info("$MOD_VERSION starting.")
        MinecraftItem.itemParser = ItemParser(server.registryAccess())
    }

    private fun afterLevelLoad(minecraftServer: MinecraftServer) {
        server = minecraftServer
        logger.info("$MOD_VERSION started.")
        archipelagoWorldData = server.overworld().dataStorage.computeIfAbsent(ArchipelagoWorldData.factory(), MOD_ID)

        apClient = APClient()
        apClient.setName(apmcData.playerName)
        apClient.connect("${apmcData.server}:${apmcData.port}")

        GoalRegister.init(apmcData)
        TrapItems.init()

        if (archipelagoWorldData.jailPlayers) {
            val overworld: ServerLevel = server.getLevel(Level.OVERWORLD)!!
            val spawn = overworld.sharedSpawnPos
            // alter the spawn box position, so it doesn't interfere with spawning
            val jail = overworld.structureManager[modResource( "spawnjail")].get()
            val jailPos = BlockPos(spawn.x + 5, 300, spawn.z + 5)
            jailCenter = BlockPos(jailPos.x + (jail.size.x / 2), jailPos.y + 1, jailPos.z + (jail.size.z / 2))
            jail.placeInWorld(overworld, jailPos, jailPos, StructurePlaceSettings(), RandomSource.create(), 2)
            server.gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, server)
            server.gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server)
            server.gameRules.getRule(GameRules.RULE_DOFIRETICK).set(false, server)
            server.gameRules.getRule(GameRules.RULE_RANDOMTICKING).set(0, server)
            server.gameRules.getRule(GameRules.RULE_DO_PATROL_SPAWNING).set(false, server)
            server.gameRules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(false, server)
            server.gameRules.getRule(GameRules.RULE_MOBGRIEFING).set(false, server)
            server.gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server)
            server.gameRules.getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(true, server)
            server.gameRules.getRule(GameRules.RULE_DOMOBLOOT).set(false, server)
            server.gameRules.getRule(GameRules.RULE_DOENTITYDROPS).set(false, server)
            overworld.dayTime = 0
        }


    }

    internal fun modResource(location: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, location)
    }

    fun loadAPMCData() {
        logger.info("Loading APMC file")
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
            logger.error("IOException: ${e.message}")
        } catch (e: NullPointerException) {
            apmcData.state = APMCData.State.MISSING
            logger.error("NullPointerException: ${e.message}")
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            apmcData.state = APMCData.State.MISSING
            logger.error("ArrayIndexOutOfBoundsException: ${e.message}")
        } catch (e: AssertionError) {
            apmcData.state = APMCData.State.MISSING
            logger.error("AssertionError: ${e.message}")
        } catch (e: IllegalArgumentException) {
            apmcData.state = APMCData.State.MISSING
            logger.error("IllegalArgumentException: ${e.message}")
        }

        if (apmcData.state == APMCData.State.MISSING) {
            logger.error("no .apmc file found. please place .apmc file in './APData/' folder. ")
        }

        logger.info("reading Locations and Items")
        if (apmcData.apLocations.isEmpty()) {
            logger.info("No locations found in APMC file, will load default locations.")
        } else {
            var locationCount = 0
            apmcData.apLocations.forEach {
                locationCount += locationRegister.newLocation(it)
            }
            logger.info("$locationCount locations loaded.")
        }

        var rewardsCount = 0
        var itemsCount = 0
        apmcData.apItems.forEach {
            val localCount = itemRegister.newItem(it)
            if (localCount == 0) {
                logger.warn("No items loaded for item ${it.id} is \"rewards\" field empty?")
            }
            rewardsCount += localCount
            itemsCount++
        }
        logger.info("$itemsCount items and $rewardsCount rewards loaded.")

    }
}
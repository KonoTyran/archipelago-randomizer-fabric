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
import dev.koifysh.randomizer.data.DataLoader
import dev.koifysh.randomizer.data.DataLoader.loadItems
import dev.koifysh.randomizer.data.DataLoader.loadLocations
import dev.koifysh.randomizer.data.items.*
import dev.koifysh.randomizer.data.locations.Advancement
import dev.koifysh.randomizer.data.locations.AdvancementLocations
import dev.koifysh.randomizer.data.recipes.GroupRecipe
import dev.koifysh.randomizer.data.recipes.ProgressiveRecipe
import dev.koifysh.randomizer.data.recipes.RecipeRewards
import dev.koifysh.randomizer.events.player.PlayerEvents
import dev.koifysh.randomizer.registries.APItemReward
import dev.koifysh.randomizer.registries.APLocation
import dev.koifysh.randomizer.registries.ItemRegister
import dev.koifysh.randomizer.registries.LocationRegister
import dev.koifysh.randomizer.registries.deserializers.APItemRewardDeserializer
import dev.koifysh.randomizer.registries.deserializers.APLocationDeserializer
import dev.koifysh.randomizer.structure.ArchipelagoStructures
import dev.koifysh.randomizer.utils.TitleQueue
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ArchipelagoRandomizer : ModInitializer {

    lateinit var gson: Gson; private set
    const val MOD_ID = "archipelago-randomizer"
    private const val MOD_VERSION = "Archipelago Randomizer v0.1.3"

    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    lateinit var apmcData: APMCData; private set
    lateinit var apClient: APClient private set
    lateinit var server: MinecraftServer private set
    lateinit var archipelagoWorldData: ArchipelagoWorldData private set

    lateinit var locationRegister: LocationRegister private set
    lateinit var itemRegister: ItemRegister private set

    lateinit var itemsHandler: MinecraftItems private set
    lateinit var compassHandler: StructureCompasses private set
    lateinit var recipeHandler: RecipeRewards private set

    var jailCenter: BlockPos = BlockPos.ZERO; private set

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

        recipeHandler = RecipeRewards()
        compassHandler = StructureCompasses()
        itemsHandler = MinecraftItems()

        locationRegister.register(
            modResource("advancement"),
            Advancement::class.java,
            advancementLocations::addLocation
        )

        itemRegister.register(
            modResource("item"),
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

        itemRegister.register(
            modResource("group_recipe"),
            GroupRecipe::class.java,
            recipeHandler::registerRecipe
        )

        itemRegister.register(
            modResource("progressive_recipe"),
            ProgressiveRecipe::class.java,
            recipeHandler::registerRecipe
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
        ServerMessageEvents.CHAT_MESSAGE.register(PlayerEvents::onChatMessage)


        // Register Commands
        CommandRegistrationCallback.EVENT.register(Connect::register)
        CommandRegistrationCallback.EVENT.register(Start::register)
        CommandRegistrationCallback.EVENT.register(Archipelago::register)
        CommandRegistrationCallback.EVENT.register(Disconnect::register)


        // Load Structures
        ArchipelagoStructures.registerStructures()

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

        TrapItems.init()
        server.gameRules.getRule(GameRules.RULE_LIMITED_CRAFTING).set(true, server)
        server.gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(true, server)
        server.gameRules.getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(false, server)
        server.setDifficulty(Difficulty.NORMAL, true)

        apClient = APClient()
        apClient.setName(apmcData.playerName)
        if (apmcData.server.isNotEmpty())
            apClient.connect("${apmcData.server}:${apmcData.port}")

        recipeHandler.initialize()
        itemsHandler.initialize()

        if (archipelagoWorldData.jailPlayers) {
            val overworld: ServerLevel = server.getLevel(Level.OVERWORLD)!!
            val spawn = overworld.sharedSpawnPos
            // alter the spawn box position, so it doesn't interfere with spawning
            val jail = overworld.structureManager[modResource("spawnjail")].get()
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

    fun loadAPMC() {
        apmcData = DataLoader.loadAPMCData()

        if (apmcData.state == APMCData.State.MISSING) {
            logger.error("no, or invalid .apmc file found. please place .apmc file in './APData/' folder.")
        }
        if (!validVersions.contains(apmcData.clientVersion)) {
            apmcData.state = APMCData.State.INVALID_VERSION
            logger.error("APMC file was generated for a different version of the client. Please update the client.")
        }

        logger.info("reading Locations and Items")

        loadLocations(apmcData.apLocations)
        loadItems(apmcData.apItems)
    }
}
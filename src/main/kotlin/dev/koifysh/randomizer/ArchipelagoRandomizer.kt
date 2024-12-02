package dev.koifysh.randomizer

import com.google.common.collect.ImmutableSet
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.koifysh.randomizer.base.goals.*
import dev.koifysh.randomizer.base.items.*
import dev.koifysh.randomizer.base.locations.Advancement
import dev.koifysh.randomizer.base.locations.AdvancementLocations
import dev.koifysh.randomizer.base.recipes.GroupRecipe
import dev.koifysh.randomizer.base.recipes.ProgressiveRecipe
import dev.koifysh.randomizer.base.recipes.RecipeRewards
import dev.koifysh.randomizer.commands.Archipelago
import dev.koifysh.randomizer.commands.Connect
import dev.koifysh.randomizer.commands.Disconnect
import dev.koifysh.randomizer.commands.Start
import dev.koifysh.randomizer.data.APMCData
import dev.koifysh.randomizer.data.ArchipelagoWorldData
import dev.koifysh.randomizer.data.DataLoader
import dev.koifysh.randomizer.events.player.PlayerEvents
import dev.koifysh.randomizer.registries.*
import dev.koifysh.randomizer.registries.deserializers.APGoalDeserializer
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
import net.minecraft.ChatFormatting
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.bossevents.CustomBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.BossEvent
import net.minecraft.world.Difficulty
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ArchipelagoRandomizer : ModInitializer {

    lateinit var gson: Gson; private set
    const val MOD_ID = "archipelago-randomizer"
    private const val MOD_VERSION = "Archipelago Randomizer v0.2.0"

    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    lateinit var apmcData: APMCData; private set
    lateinit var apClient: APClient private set
    lateinit var server: MinecraftServer private set
    lateinit var archipelagoWorldData: ArchipelagoWorldData private set

    val locationRegister = LocationRegister()
    val itemRewardRegister = ItemRegister()
    val goalRegister = GoalRegister()

    lateinit var connectionInfoBar: CustomBossEvent private set

    lateinit var compassHandler: StructureCompasses private set
    lateinit var recipeHandler: RecipeRewards private set
    lateinit var goalHandler: BuiltInGoals private set

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

        recipeHandler = RecipeRewards()
        compassHandler = StructureCompasses()
        goalHandler = BuiltInGoals()

        // register our location handlers
        locationRegister.register(
            modResource("advancement"),
            Advancement::class.java,
            advancementLocations::addLocation
        )

        // register our item reward handlers
        itemRewardRegister.register(modResource("item"), ItemReward::class.java)
        itemRewardRegister.register(modResource("trap"), TrapItem::class.java)
        itemRewardRegister.register(
            modResource("structure_compass"),
            StructureCompass::class.java,
            compassHandler::registerCompass
        )
        itemRewardRegister.register(modResource("group_recipe"), GroupRecipe::class.java, recipeHandler::registerRecipe)
        itemRewardRegister.register(
            modResource("progressive_recipe"),
            ProgressiveRecipe::class.java,
            recipeHandler::registerRecipe
        )
        itemRewardRegister.register(modResource("xp"), XPReward::class.java)
        itemRewardRegister.register(modResource("goal_dragon_egg"), EggShardReward::class.java)

        // register our goal handlers
        goalRegister.register(modResource("advancement"), AdvancementGoal::class.java, goalHandler::initializeGoal)
        goalRegister.register(modResource("egg_shards"), EggShardGoal::class.java, goalHandler::initializeGoal)
        goalRegister.register(modResource("dragon_boss"), EnderDragonGoal::class.java, goalHandler::initializeGoal)
        goalRegister.register(modResource("wither_boss"), WitherBossGoal::class.java, goalHandler::initializeGoal)

        // load apmc file
        val builder = GsonBuilder()
        builder.registerTypeAdapter(ResourceLocation::class.java, ResourceLocation.Serializer())
        builder.registerTypeAdapter(APLocation::class.java, APLocationDeserializer)
        builder.registerTypeAdapter(APItemReward::class.java, APItemRewardDeserializer)
        builder.registerTypeAdapter(APGoal::class.java, APGoalDeserializer)
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
        ItemReward.itemParser = ItemParser(server.registryAccess())
        val infoBarText = if (apmcData.state == APMCData.State.VALID) "Not Connected to Archipelago" else "Invalid APMC File"
        connectionInfoBar = server.customBossEvents.get(
            modResource("connection_info")
        ) ?: server.customBossEvents.create(modResource("connection_info"), Component.literal(infoBarText).withStyle(ChatFormatting.RED))
        connectionInfoBar.color = BossEvent.BossBarColor.RED
        connectionInfoBar.max = 1
        connectionInfoBar.value = 1
        connectionInfoBar.overlay = BossEvent.BossBarOverlay.PROGRESS
        connectionInfoBar.isVisible = true
    }

    private fun afterLevelLoad(minecraftServer: MinecraftServer) {
        server = minecraftServer
        logger.info("$MOD_VERSION started.")
        archipelagoWorldData = server.overworld().dataStorage.computeIfAbsent(ArchipelagoWorldData.factory(), MOD_ID)

        server.gameRules.getRule(GameRules.RULE_LIMITED_CRAFTING).set(true, server)
        server.gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(true, server)
        server.gameRules.getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(false, server)
        server.setDifficulty(Difficulty.NORMAL, true)



        apClient = APClient()
        apClient.setName(apmcData.playerName)
        if (apmcData.server.isNotEmpty())
            apClient.connect("${apmcData.server}:${apmcData.port}")

        recipeHandler.initialize()
        TrapItems.init()
        goalRegister.initializeGoals()
        apClient.checkLocations(ArrayList(archipelagoWorldData.getCompletedLocations()))

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
            return
        }
        else if (!validVersions.contains(apmcData.clientVersion)) {
            apmcData.state = APMCData.State.INVALID_VERSION
            logger.error("APMC file was generated for a different version of the client. Please update the client.")
        }

        if (apmcData.clientVersion > 9) { // our goals should be loaded from the file
            logger.info("reading Locations and Items")
            DataLoader.loadLocations(apmcData.apLocations)
            DataLoader.loadItems(apmcData.apItems)
            DataLoader.loadGoals(apmcData.apGoals)
        } else {
            // we need to generate goals based on the data from the old format.
            if (apmcData.eggShardsRequired > 0)
                apmcData.apGoals.add(EggShardGoal(apmcData.eggShardsRequired))
            if (apmcData.advancementsRequired > 0)
                apmcData.apGoals.add(AdvancementGoal(apmcData.advancementsRequired))
            if (apmcData.requiredBosses == APMCData.Bosses.BOTH || apmcData.requiredBosses == APMCData.Bosses.ENDER_DRAGON) {
                val dragonGoal = EnderDragonGoal()
                if (apmcData.eggShardsRequired > 0)
                    dragonGoal.requirements.add(modResource("egg_shards"))
                if (apmcData.advancementsRequired > 0)
                    dragonGoal.requirements.add(modResource("advancement"))
                apmcData.apGoals.add(dragonGoal)
            }
            if (apmcData.requiredBosses == APMCData.Bosses.BOTH || apmcData.requiredBosses == APMCData.Bosses.WITHER) {
                val witherGoal = WitherBossGoal()
                if (apmcData.eggShardsRequired > 0)
                    witherGoal.requirements.add(modResource("egg_shards"))
                if (apmcData.advancementsRequired > 0)
                    witherGoal.requirements.add(modResource("advancement"))
                apmcData.apGoals.add(witherGoal)
            }
            DataLoader.loadGoals(apmcData.apGoals)
        }
    }
}
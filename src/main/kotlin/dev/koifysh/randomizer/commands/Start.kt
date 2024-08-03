package dev.koifysh.randomizer.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import dev.koifysh.archipelago.ClientStatus
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.stats.Stats
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

object Start {

    //build our command structure and submit it
    @Suppress("UNUSED_PARAMETER")
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, buildContext: CommandBuildContext, selection: Commands.CommandSelection) {
        ArchipelagoRandomizer.logger.debug("Registering start command")
        dispatcher.register(
            Commands.literal("start") //base slash command is "start"
                .executes { context: CommandContext<CommandSourceStack> ->
                    start(context, false)
                }
        )

        dispatcher.register(
            Commands.literal("force-start") //base slash command is "force start"
                .executes { context: CommandContext<CommandSourceStack> ->
                    start(context, true)
                }
        )
    }

    private fun start(commandSourceCommandContext: CommandContext<CommandSourceStack>, force: Boolean): Int {
        if (!ArchipelagoRandomizer.apClient.isConnected && !force) {
            commandSourceCommandContext.source.sendFailure(Component.literal("Please connect to the Archipelago server before starting."))
            return 0
        }
        if (!ArchipelagoRandomizer.archipelagoWorldData.jailPlayers) {
            commandSourceCommandContext.source.sendFailure(Component.literal("The game has already started! what are you doing? START PLAYING!"))
            return 0
        }

        Utils.sendMessageToAll("GO!")
        if (ArchipelagoRandomizer.apClient.isConnected) {
            ArchipelagoRandomizer.apClient.setGameState(ClientStatus.CLIENT_PLAYING)
        }

        ArchipelagoRandomizer.archipelagoWorldData.jailPlayers = false
        val server = ArchipelagoRandomizer.server
        val overworld = server.getLevel(Level.OVERWORLD)!!

        val spawn = overworld.sharedSpawnPos
        val jailStruct = overworld.structureManager[ResourceLocation.fromNamespaceAndPath(ArchipelagoRandomizer.MOD_ID, "spawnjail")].get()
        val jailPos = BlockPos(spawn.x + 5, 300, spawn.z + 5)
        for (blockPos in BlockPos.betweenClosed(jailPos, jailPos.offset(jailStruct.size))) {
            overworld.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS)
        }
        server.gameRules.getRule(GameRules.RULE_DAYLIGHT).set(true, server)
        server.gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(true, server)
        server.gameRules.getRule(GameRules.RULE_DOFIRETICK).set(true, server)
        server.gameRules.getRule(GameRules.RULE_RANDOMTICKING).set(3, server)
        server.gameRules.getRule(GameRules.RULE_DO_PATROL_SPAWNING).set(true, server)
        server.gameRules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(true, server)
        server.gameRules.getRule(GameRules.RULE_MOBGRIEFING).set(true, server)
        server.gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(true, server)
        server.gameRules.getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(false, server)
        server.gameRules.getRule(GameRules.RULE_DOMOBLOOT).set(true, server)
        server.gameRules.getRule(GameRules.RULE_DOENTITYDROPS).set(true, server)

        server.execute {
            for (player in server.playerList.players) {
                player.foodData.eat(20, 20f)
                player.health = 20f
                player.inventory.clearContent()
                player.resetStat(Stats.CUSTOM[Stats.TIME_SINCE_REST])
                player.teleportTo(spawn.x.toDouble(), spawn.y.toDouble(), spawn.z.toDouble())

//                ArchipelagoRandomizer.itemManager.catchUpPlayer(player)
//                if (APRandomizer.isConnected()) {
//                    for (iStack in APRandomizer.getAP().getSlotData().startingItemStacks) {
//                        Utils.giveItemToPlayer(player, iStack.copy())
//                    }
//                }
            }
        }
        return 1
    }
}
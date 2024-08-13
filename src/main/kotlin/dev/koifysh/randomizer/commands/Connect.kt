package dev.koifysh.randomizer.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import dev.koifysh.randomizer.APClient
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.rewards.APMCData
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import java.net.URISyntaxException
import net.minecraft.commands.CommandBuildContext

object Connect {

    //build our command structure and submit it
    @Suppress("UNUSED_PARAMETER")
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, buildContext: CommandBuildContext, commandSelection: Commands.CommandSelection) {
        ArchipelagoRandomizer.logger.debug("Registering connect command")
        dispatcher.register(
            Commands.literal("connect") //base slash command is "connect"
                .executes {
                    connectToAPServer(
                        ArchipelagoRandomizer.apmcData.server,
                        ArchipelagoRandomizer.apmcData.port
                    )
                } //take the first argument as a string and name it "Address"
                .then(Commands.argument("Address", StringArgumentType.string())
                    .executes { context ->
                        connectToAPServer(StringArgumentType.getString(context, "Address"))
                    }
                    .then(Commands.argument("Port", IntegerArgumentType.integer())
                        .executes { context ->
                            connectToAPServer(
                                StringArgumentType.getString(context, "Address"),
                                IntegerArgumentType.getInteger(context, "Port")
                            )
                        }
                        .then(
                            Commands.argument("Password", StringArgumentType.string())
                                .executes { context ->
                                    connectToAPServer(
                                        StringArgumentType.getString(context, "Address"),
                                        IntegerArgumentType.getInteger(context, "Port"),
                                        StringArgumentType.getString(context, "Password")
                                    )
                                }
                        )
                    )
                )
        )
    }

    private fun connectToAPServer(
        hostname: String,
        port: Int = -1,
        password: String = "",
    ): Int {
        val data: APMCData = ArchipelagoRandomizer.apmcData
        when (data.state) {
            APMCData.State.VALID -> {
                val client: APClient = ArchipelagoRandomizer.apClient
                client.setName(data.playerName)
                client.password = password
                val address: String = if (port == -1) hostname else "$hostname:$port"
                Utils.sendMessageToAll("Connecting to Archipelago server at $address")
                try {
                    client.connect(address)
                } catch (e: URISyntaxException) {
                    Utils.sendMessageToAll("Malformed address $address: ${e.message}")
                }
            }
            APMCData.State.MISSING -> {
                Utils.sendMessageToAll("no .apmc file found. please stop the server,  place .apmc file in './APData/', delete the world folder, then relaunch the server.")
            }
            APMCData.State.INVALID_VERSION -> {
                Utils.sendMessageToAll("APMC data file wrong version.")
            }
            APMCData.State.INVALID_SEED -> Utils.sendMessageToAll("Current Minecraft world has been used for a previous game. please stop server, delete the world and relaunch the server.")

        }
        return 1
    }
}
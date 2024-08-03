package dev.koifysh.randomizer.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.koifysh.archipelago.helper.DeathLink
import dev.koifysh.randomizer.utils.TitleQueue
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import dev.koifysh.randomizer.ArchipelagoRandomizer.apClient
import net.minecraft.commands.CommandBuildContext

object Archipelago {

    //build our command structure and submit it
    @Suppress("UNUSED_PARAMETER")
   fun register(dispatcher: CommandDispatcher<CommandSourceStack>, buildContext: CommandBuildContext, selection: Commands.CommandSelection) {
        dispatcher.register(
            Commands.literal("ap") //base slash command is "ap"
                //First sub-command to set/retrieve death link status
                .then(
                    Commands.literal("death-link")
                        .executes(::queryDeathLink)
                        .then(
                            Commands.argument("value", BoolArgumentType.bool())
                                .executes(::setDeathLink)
                        )
                ) //Second sub-command to set/retrieve MC35 status
                .then(
                    Commands.literal("mc35")
                        .executes(::queryMC35)
                        .then(
                            Commands.argument("value", BoolArgumentType.bool())
                                .executes(::setMC35)
                        )
                ) //third sub-command to stop title queue
                .then(
                    Commands.literal("clearTitleQueue")
                        .executes {
                            clearTitleQueue()
                            return@executes 1
                        }

                )
        )
    }

    private fun clearTitleQueue() {
        Utils.sendMessageToAll("Title Queue Cleared")
        TitleQueue.clearQueue()
    }

    private fun queryDeathLink(context: CommandContext<CommandSourceStack>): Int {
        if (!apClient.isConnected) {
            context.source.sendFailure(Component.literal("Must be connected to an AP server to use this command"))
            return 0
        }
        val enabledString = if (apClient.slotData.MC35) "enabled" else "disabled"
        context.source.sendSuccess({ Component.literal("DeathLink is $enabledString") }, false)
        return 1
    }

    private fun setDeathLink(context: CommandContext<CommandSourceStack>): Int {
        if (!apClient.isConnected) {
            context.source.sendFailure(Component.literal("Must be connected to an AP server to use this command"))
            return 0
        }

        val enabled = BoolArgumentType.getBool(context, "value")
        apClient.slotData.deathlink = enabled
        DeathLink.setDeathLinkEnabled(enabled)
        val enabledString = if (apClient.slotData.MC35) "enabled" else "disabled"
        context.source.sendSuccess({ Component.literal("DeathLink is $enabledString") }, false)
        return 1
    }

    private fun queryMC35(source: CommandContext<CommandSourceStack>): Int {
        if (!apClient.isConnected) {
            source.source.sendFailure(Component.literal("Must be connected to an AP server to use this command"))
            return 0
        }

        val enabled = if (apClient.slotData.MC35) "enabled" else "disabled"
        source.source.sendSuccess( {
            Component.literal(
                "MC35 is $enabled"
            )
        }, false)
        return 1
    }

    private fun setMC35(source: CommandContext<CommandSourceStack>): Int {
        if (apClient.isConnected) {
            source.source.sendFailure(Component.literal("Must be connected to an AP server to use this command"))
            return 0
        }

        apClient.slotData.MC35 = BoolArgumentType.getBool(source, "value")
        if (apClient.slotData.MC35) {
            apClient.addTag("MC35")
        } else {
            apClient.removeTag("MC35")
        }

        val enabledString = if (apClient.slotData.MC35) "enabled" else "disabled"
        source.source.sendSuccess({ Component.literal("MC35 is $enabledString") }, false)
        return 1
    }
}

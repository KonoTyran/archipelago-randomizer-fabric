package dev.koifysh.randomizer.commands

import com.mojang.brigadier.CommandDispatcher
import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object Disconnect {

    //build our command structure and submit it
    @Suppress("UNUSED_PARAMETER")
   fun register(dispatcher: CommandDispatcher<CommandSourceStack>, buildContext: CommandBuildContext, selection: Commands.CommandSelection) {
        dispatcher.register(
            Commands.literal("disconnect")
                .executes { _ -> disconnect() }
        )

    }

    private fun disconnect(): Int {
        ArchipelagoRandomizer.apClient.disconnect()
        return 1
    }
}
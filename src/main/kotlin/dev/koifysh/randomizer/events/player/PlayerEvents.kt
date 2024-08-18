package dev.koifysh.randomizer.events.player

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.apmcData
import dev.koifysh.randomizer.ArchipelagoRandomizer.server
import dev.koifysh.randomizer.data.APMCData
import dev.koifysh.randomizer.base.items.StructureCompasses.Companion.refreshCompasses
import dev.koifysh.randomizer.utils.Utils
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.level.GameType

object PlayerEvents {
    fun onPlayerJoin(
        packetListener: ServerGamePacketListenerImpl,
        packetSender: PacketSender,
        minecraftServer: MinecraftServer,
    ) {
        val player = packetListener.player
        ArchipelagoRandomizer.advancementLocations.grantCompletedAdvancements(player)
        ArchipelagoRandomizer.recipeHandler.syncTrackingAdvancements(player)
        ArchipelagoRandomizer.itemRewardRegister.catchUpPlayer(packetListener.player)

        ArchipelagoRandomizer.goalRegister.addPlayerToBossBar(player)
        ArchipelagoRandomizer.connectionInfoBar.addPlayer(player)

        if (apmcData.race) {
            player.setGameMode(GameType.SURVIVAL)
        }

        when (apmcData.state) {
            APMCData.State.MISSING -> {
                Utils.sendMessageToAll("No APMC file found, please only start the server via the APMC file.")
                return
            }
            APMCData.State.INVALID_VERSION -> {
                Utils.sendMessageToAll("This Seed was generated using an incompatible randomizer version.")
                return
            }
            APMCData.State.INVALID_SEED -> {
                Utils.sendMessageToAll("Supplied APMC file does not match world loaded. something went very wrong here.")
                return
            }
            else -> {
                player.awardRecipes(server.recipeManager.recipes)
                player.resetRecipes(ArchipelagoRandomizer.recipeHandler.restrictedRecipes)



                if (ArchipelagoRandomizer.archipelagoWorldData.jailPlayers) {
                    val jail: BlockPos = ArchipelagoRandomizer.jailCenter
                    player.teleportTo(jail.x.toDouble(), jail.y.toDouble(), jail.z.toDouble())
                    player.setGameMode(GameType.SURVIVAL)
                }
            }
        }
    }

    fun onPlayerChangeWorld(player: ServerPlayer, origin: ServerLevel, destination: ServerLevel) {
        player.refreshCompasses()
    }

    fun onChatMessage(playerChatMessage: PlayerChatMessage, player: ServerPlayer, bound: ChatType.Bound) {
        if (!ArchipelagoRandomizer.apClient.isConnected) return

        val message = playerChatMessage.decoratedContent().string
        if (message.startsWith("!")) ArchipelagoRandomizer.apClient.sendChat(message)
        else ArchipelagoRandomizer.apClient.sendChat("(" + player.displayName!!.string + ") " + message)
    }
}
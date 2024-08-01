package dev.koifysh.randomizer.utils

import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer

object TitleUtils {
    fun resetTitle(players: Collection<ServerPlayer>) {
        val stitlepacket = ClientboundClearTitlesPacket(true)

        for (serverplayerentity in players) {
            serverplayerentity.connection.send(stitlepacket)
        }
    }

    fun showTitle(players: Collection<ServerPlayer>, title: Component?, subtitle: Component?) {
        val subtitleTextPacket = ClientboundSetSubtitleTextPacket(subtitle)
        val titleTextPacket = ClientboundSetTitleTextPacket(title)
        for (serverPlayerEntity in players) {
            serverPlayerEntity.connection.send(subtitleTextPacket)
            serverPlayerEntity.connection.send(titleTextPacket)
        }
    }

    fun showActionBar(players: Collection<ServerPlayer>, actionBarText: Component?) {
        val actionBarTextPacket = ClientboundSetActionBarTextPacket(actionBarText)
        for (serverPlayerEntity in players) {
            serverPlayerEntity.connection.send(actionBarTextPacket)
        }
    }

    fun setTimes(players: Collection<ServerPlayer>, fadeIn: Int, stay: Int, fadeOut: Int) {
        val animationPacket = ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut)
        for (serverPlayerEntity in players) {
            serverPlayerEntity.connection.send(animationPacket)
        }
    }
}

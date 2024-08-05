package dev.koifysh.randomizer.utils

import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer

object TitleUtils {
    fun Iterable<ServerPlayer>.resetTitle() {
        val clearTitlePacket = ClientboundClearTitlesPacket(true)
        for (player in this) {
            player.connection.send(clearTitlePacket)
        }
    }

    fun Iterable<ServerPlayer>.showTitle(title: Component, subtitle: Component) {
        val subtitleTextPacket = ClientboundSetSubtitleTextPacket(subtitle)
        val titleTextPacket = ClientboundSetTitleTextPacket(title)
        for (player in this) {
            player.connection.send(subtitleTextPacket)
            player.connection.send(titleTextPacket)
        }
    }

    fun Iterable<ServerPlayer>.showActionBar(actionBarText: Component) {
        val actionBarTextPacket = ClientboundSetActionBarTextPacket(actionBarText)
        for (player in this) {
            player.connection.send(actionBarTextPacket)
        }
    }

    fun Iterable<ServerPlayer>.setTitleTimes(fadeIn: Int, stay: Int, fadeOut: Int) {
        val animationPacket = ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut)
        for (player in this) {
            player.connection.send(animationPacket)
        }
    }
}

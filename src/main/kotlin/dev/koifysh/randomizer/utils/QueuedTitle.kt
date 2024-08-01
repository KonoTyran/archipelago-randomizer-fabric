package dev.koifysh.randomizer.utils

import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class QueuedTitle(
    players: List<ServerPlayer>,
    private val fadeIn: Int,
    private val stay: Int,
    private val fadeOut: Int,
    private val subTitle: Component,
    private val title: Component
) {
    val ticks: Int = fadeIn + stay + fadeOut + 20
    private val players: List<ServerPlayer> = players
    private var chatMessage: Component? = null

    constructor(
        players: List<ServerPlayer>,
        fadeIn: Int,
        stay: Int,
        fadeOut: Int,
        subTitle: Component,
        title: Component,
        chatMessage: Component?
    ) : this(players, fadeIn, stay, fadeOut, subTitle, title) {
        this.chatMessage = chatMessage
    }


    fun sendTitle() {
        ArchipelagoRandomizer.server.execute {
            TitleUtils.setTimes(players, fadeIn, stay, fadeOut)
            TitleUtils.showTitle(players, title, subTitle)
            if (chatMessage != null) {
                Utils.sendMessageToAll(chatMessage)
            }
        }
    }
}

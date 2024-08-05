package dev.koifysh.randomizer.utils

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.TitleUtils.setTitleTimes
import dev.koifysh.randomizer.utils.TitleUtils.showTitle
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class QueuedTitle(
    private val players: Iterable<ServerPlayer>,
    private val fadeIn: Int,
    private val stay: Int,
    private val fadeOut: Int,
    private val subTitle: Component,
    private val title: Component
) {

    val ticks: Int = fadeIn + stay + fadeOut + 20
    private var chatMessage: Component? = null

    constructor(
        players: Iterable<ServerPlayer>,
        fadeIn: Int,
        stay: Int,
        fadeOut: Int,
        subTitle: Component,
        title: Component,
        chatMessage: Component
    ) : this(players, fadeIn, stay, fadeOut, subTitle, title) {
        this.chatMessage = chatMessage
    }


    fun ServerPlayer.sendTitle() {
        ArchipelagoRandomizer.server.execute {
            players.setTitleTimes(fadeIn, stay, fadeOut)
            players.showTitle(title, subTitle)
            if (chatMessage == null) return@execute
            Utils.sendMessageToAll(chatMessage!!)
        }
    }
}

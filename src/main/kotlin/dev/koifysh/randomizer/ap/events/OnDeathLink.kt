package dev.koifysh.randomizer.ap.events

import dev.koifysh.archipelago.events.ArchipelagoEventListener
import dev.koifysh.archipelago.events.DeathLinkEvent
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.DeathLinkDamage
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.world.level.GameRules

object OnDeathLink {
    @ArchipelagoEventListener
    fun onDeath(event: DeathLinkEvent) {
        if (!ArchipelagoRandomizer.apClient.slotData.deathlink) return

        val showDeathMessages: GameRules.BooleanValue =
            ArchipelagoRandomizer.server.gameRules.getRule(GameRules.RULE_SHOWDEATHMESSAGES)
        val showDeaths: Boolean = showDeathMessages.get()
        if (showDeaths) {
            val cause: String = event.cause
            if (cause != null && cause.isNotBlank()) Utils.sendMessageToAll(event.cause)
            else Utils.sendMessageToAll("This Death brought to you by " + event.source)
        }
        showDeathMessages.set(false, ArchipelagoRandomizer.server)
        for (player in ArchipelagoRandomizer.server.playerList.players) {
            player.hurt(DeathLinkDamage(), Float.MAX_VALUE)
        }
        showDeathMessages.set(showDeaths, ArchipelagoRandomizer.server)
    }
}

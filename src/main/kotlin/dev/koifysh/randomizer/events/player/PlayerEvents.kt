package dev.koifysh.randomizer.events.player

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.data.items.StructureCompasses.Companion.refreshCompasses
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

object PlayerEvents {
    fun onPlayerJoin(packetListener: ServerGamePacketListenerImpl, packetSender: PacketSender, minecraftServer: MinecraftServer) {
        ArchipelagoRandomizer.advancementLocations.syncAllAdvancements()
//        ArchipelagoRandomizer.itemHandler.catchUpPlayer(packetListener.player)
    }

    fun onPlayerChangeWorld(player: ServerPlayer, origin: ServerLevel, destination: ServerLevel) {
        player.refreshCompasses()
    }

    fun onUseItem(player: Player, level: Level, interactionHand: InteractionHand): InteractionResultHolder<ItemStack> {
        player.sendSystemMessage(Component.literal("You used an item!"))
    }
}
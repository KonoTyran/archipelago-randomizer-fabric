package dev.koifysh.randomizer.utils

import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.koifysh.archipelago.Print.APPrint
import dev.koifysh.archipelago.Print.APPrintColor
import dev.koifysh.archipelago.Print.APPrintType
import dev.koifysh.archipelago.flags.NetworkItem
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.ArchipelagoRandomizer.server
import dev.koifysh.randomizer.utils.TitleUtils.setTitleTimes
import dev.koifysh.randomizer.utils.TitleUtils.showActionBar
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.phys.Vec3
import java.awt.Color
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

object Utils {

    /**
     * send a message to whoever ran the command.
     * @param source command source to send the message.
     * @param message Message to send
     */
    fun SendMessage(source: CommandSourceStack, message: String) {
        try {
            val player: ServerPlayer = source.playerOrException
            player.sendSystemMessage(Component.literal(message))
        } catch (e: CommandSyntaxException) {
            source.server.sendSystemMessage(Component.literal(message))
        }
    }

    fun sendMessageToAll(message: String) {
        sendMessageToAll(Component.literal(message))
    }

    fun sendMessageToAll(message: Component) {
        //tell the server to send the message in a thread safe way.
        server.execute {
            server.playerList.broadcastSystemMessage(message, false)
        }
    }

    fun sendFancyMessageToAll(apPrint: APPrint) {
        val message = apPrintToTextComponent(apPrint)

        //tell the server to send the message in a thread safe way.
        server.execute {
            server.playerList.broadcastSystemMessage(message, false)
        }
    }

    private fun apPrintToTextComponent(apPrint: APPrint): Component {
        val isMe = apPrint.receiving == ArchipelagoRandomizer.apClient.slot

        val message: MutableComponent = Component.empty()
        for (part in apPrint.parts) {
            logger.trace("part[]: {}, {}, {}", part.text, part.color, part.type)
            //no default color was sent so use our own coloring.
            //no default color was sent so use our own coloring.
            var color = if (isMe) Color.PINK else Color.WHITE
            val bold = false
            var underline = false

            if (part.color == APPrintColor.none) {
                if (ArchipelagoRandomizer.apClient.myName.equals(part.text)) {
                    color = Color.decode("#EE00EE")
                    underline = true
                } else if (part.type == APPrintType.playerID) {
                    color = Color.decode("#FAFAD2")
                } else if (part.type == APPrintType.locationID) {
                    color = Color.decode("#00FF7F")
                } else if (part.type == APPrintType.itemID) {
                    color = if ((part.flags and NetworkItem.ADVANCEMENT) == NetworkItem.ADVANCEMENT) {
                        Color.decode("#AF99EF") // advancement
                    } else if ((part.flags and NetworkItem.USEFUL) == NetworkItem.USEFUL) {
                        Color.decode("#6D8BE8") // useful
                    } else if ((part.flags and NetworkItem.TRAP) == NetworkItem.TRAP) {
                        Color.decode("#FA8072") // trap
                    } else {
                        Color.decode("#00EEEE")
                    }
                }
            } else {
                color = part.color.color
            }

            //blank out the first two bits because minecraft doesn't deal with alpha values
            val iColor: Int = color.rgb and (0xFF shl 24).inv()
            val style = Style.EMPTY.withColor(iColor).withBold(bold).withUnderlined(underline)

            message.append(Component.literal(part.text).withStyle(style))
        }
        return message
    }

    fun sendTitleToAll(title: Component, subTitle: Component, fadeIn: Int, stay: Int, fadeOut: Int) {
        server.execute {
            TitleQueue.queueTitle(
                QueuedTitle(
                    server.playerList.players,
                    fadeIn,
                    stay,
                    fadeOut,
                    subTitle,
                    title
                )
            )
        }
    }

    fun Iterable<ServerPlayer>.sendTitle(
        title: Component,
        subTitle: Component,
        chatMessage: Component,
        fadeIn: Int,
        stay: Int,
        fadeOut: Int,
    ) {
        server.execute {
            TitleQueue.queueTitle(
                QueuedTitle(
                    this,
                    fadeIn,
                    stay,
                    fadeOut,
                    subTitle,
                    title,
                    chatMessage
                )
            )
        }
    }

    fun Iterable<ServerPlayer>.sendActionBar(actionBarMessage: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        server.execute {
            this.setTitleTimes(fadeIn, stay, fadeOut)
            this.showActionBar(Component.literal(actionBarMessage))
        }
    }

    fun ServerPlayer.sendActionBar(actionBarMessage: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        server.execute {
            listOf(this).setTitleTimes( fadeIn, stay, fadeOut)
            listOf(this).showActionBar(Component.literal(actionBarMessage))
        }
    }

    fun Iterable<ServerPlayer>.playSoundToAll(sound: SoundEvent) {
        server.execute {
            for (player in server.playerList.players) {
                player.playNotifySound(sound, SoundSource.MASTER, 1f, 1f)
            }
        }
    }

    fun getStructureWorld(structureTag: TagKey<Structure>): ResourceKey<Level> {
        val structureName = getAPStructureName(structureTag)
        //fetch what structures are where from our APMC data.
        val structures: HashMap<String, String> = ArchipelagoRandomizer.apmcData.structures
        for ((key, value) in structures) {
            if (value == structureName) {
                if (key.contains("Overworld")) {
                    return Level.OVERWORLD
                }
                if (key.contains("Nether")) {
                    return Level.NETHER
                }
                if (key.contains("The End")) {
                    return Level.END
                }
            }
        }

        return Level.OVERWORLD
    }

    fun getAPStructureName(structureTag: TagKey<Structure>): String {
        return when (structureTag.location().toString()) {
            "${ArchipelagoRandomizer.MOD_ID}:village" -> "Village"
            "${ArchipelagoRandomizer.MOD_ID}:end_city" -> "End City"
            "${ArchipelagoRandomizer.MOD_ID}:pillager_outpost" -> "Pillager Outpost"
            "${ArchipelagoRandomizer.MOD_ID}:fortress" -> "Nether Fortress"
            "${ArchipelagoRandomizer.MOD_ID}:bastion_remnant" -> "Bastion Remnant"
            else -> structureTag.location().path.lowercase(Locale.getDefault())
        }
    }

    fun getRandomPosition(pos: Vec3, radius: Int): Vec3 {
        val a = Math.random() * Math.PI * 2
        val b = Math.random() * Math.PI / 2
        val x: Double = radius * cos(a) * sin(b) + pos.x
        val z: Double = radius * sin(a) * sin(b) + pos.z
        val y: Double = radius * cos(b) + pos.y
        return Vec3(x, y, z)
    }

    fun giveItemToPlayer(player: ServerPlayer, itemStack: ItemStack) {
        val flag: Boolean = player.inventory.add(itemStack)
        if (flag && itemStack.isEmpty) {
            itemStack.count = 1
            player.drop(itemStack, false)?.makeFakeItem()
            player.level().playSound(
                null,
                player.x,
                player.y,
                player.z,
                SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                0.2f,
                ((player.random.nextFloat() - player.random.nextFloat()) * 0.7f + 1.0f) * 2.0f
            )
            player.inventoryMenu.broadcastChanges()
        } else {
            val itemEntity: ItemEntity? = player.drop(itemStack, false)
            if (itemEntity != null) {
                itemEntity.setNoPickUpDelay()
                itemEntity.setTarget(player.uuid)
            }
        }
    }

    fun ItemStack.setItemLore(itemLore: Collection<String>) {
        setItemLore(itemLore.map { Component.literal(it).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false)) })
    }

    fun ItemStack.setItemLore(itemLore: List<Component>) {
        this.set(DataComponents.LORE, ItemLore(itemLore))
    }
}

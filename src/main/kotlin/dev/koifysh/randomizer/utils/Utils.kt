package dev.koifysh.randomizer.utils

import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.koifysh.archipelago.Print.APPrint
import dev.koifysh.archipelago.Print.APPrintColor
import dev.koifysh.archipelago.Print.APPrintPart
import dev.koifysh.archipelago.Print.APPrintType
import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.phys.Vec3
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Color
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.cos
import kotlin.math.sin

object Utils {

    // Directly reference a log4j logger.
    private val LOGGER: Logger = LogManager.getLogger()

    /**
     * @param source command source to send the message.
     * @param Message Message to send
     * send a message to whoever ran the command.
     */
    private val server: MinecraftServer = ArchipelagoRandomizer.server

    fun SendMessage(source: CommandSourceStack, Message: String) {
        try {
            val player: ServerPlayer = source.playerOrException
            player.sendSystemMessage(Component.literal(Message))
        } catch (e: CommandSyntaxException) {
            source.server.sendSystemMessage(Component.literal(Message))
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

    fun apPrintToTextComponent(apPrint: APPrint): Component {
        val isMe = apPrint.receiving == ArchipelagoRandomizer.apClient.slot

        val message: MutableComponent = Component.literal("")
        var i = 0
        while (apPrint.parts.size > i) {
            val part: APPrintPart = apPrint.parts.get(i)
            LOGGER.trace("part[{}]: {}, {}, {}", i, part.text, part.color, part.type)
            //no default color was sent so use our own coloring.
            var color: Color = if (isMe) Color.RED else Color.WHITE
            var bold = false
            var underline = false

            if (part.color == APPrintColor.none) {
                if (ArchipelagoRandomizer.apClient.myName.equals(part.text)) {
                    color = APPrintColor.gold.color
                    bold = true
                } else if (part.type == APPrintType.playerID) {
                    color = APPrintColor.yellow.color
                } else if (part.type == APPrintType.locationID) {
                    color = APPrintColor.green.color
                } else if (part.type == APPrintType.itemID) {
                    color = APPrintColor.cyan.color
                }
            } else if (part.color == APPrintColor.underline) underline = true
            else if (part.color == APPrintColor.bold) bold = true
            else color = part.color.color

            //blank out the first two bits because minecraft doesn't deal with alpha values
            val iColor: Int = color.getRGB() and (0xFF shl 24).inv()
            val style = Style.EMPTY.withColor(iColor).withBold(bold).withUnderlined(underline)

            message.append(Component.literal(part.text).withStyle(style))
            ++i
        }
        return message
    }

    fun sendTitleToAll(title: Component, subTitle: Component, fadeIn: Int, stay: Int, fadeOut: Int) {
        server.execute {
            TitleQueue.queueTitle(
                QueuedTitle(
                    server.getPlayerList().getPlayers(),
                    fadeIn,
                    stay,
                    fadeOut,
                    subTitle,
                    title
                )
            )
        }
    }

    fun sendTitleToAll(
        title: Component,
        subTitle: Component,
        chatMessage: Component,
        fadeIn: Int,
        stay: Int,
        fadeOut: Int
    ) {
        server.execute {
            TitleQueue.queueTitle(
                QueuedTitle(
                    server.playerList.players,
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

    fun sendActionBarToAll(actionBarMessage: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        server.execute {
            TitleUtils.setTimes(server.playerList.players, fadeIn, stay, fadeOut)
            TitleUtils.showActionBar(server.playerList.players, Component.literal(actionBarMessage))
        }
    }

    fun sendActionBarToPlayer(player: ServerPlayer, actionBarMessage: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        server.execute {
            TitleUtils.setTimes(listOf(player), fadeIn, stay, fadeOut)
            TitleUtils.showActionBar(listOf(player), Component.literal(actionBarMessage))
        }
    }

    fun playSoundToAll(sound: SoundEvent) {
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

    fun giveItemToPlayer(player: ServerPlayer, itemstack: ItemStack) {
        val flag: Boolean = player.inventory.add(itemstack)
        if (flag && itemstack.isEmpty) {
            itemstack.count = 1
            player.drop(itemstack, false)?.makeFakeItem()
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
            val itemEntity: ItemEntity? = player.drop(itemstack, false)
            if (itemEntity != null) {
                itemEntity.setNoPickUpDelay()
                itemEntity.setTarget(player.uuid)
            }
        }
    }
}

package dev.koifysh.randomizer.base.items

import com.mojang.brigadier.StringReader
import dev.koifysh.randomizer.registries.APItemReward
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.commands.arguments.item.ItemInput
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

data class ItemReward(
    val item: String,
    val amount: Int,
) : APItemReward() {

    companion object {
        lateinit var itemParser: ItemParser; internal set
    }

    @Transient
    private lateinit var itemStack: ItemStack

    fun init() {
        if (this::itemStack.isInitialized) return
        val itemResult = itemParser.parse(StringReader(item))
        itemStack = ItemInput(itemResult.item, itemResult.components).createItemStack(amount, false)
    }

    override fun grantPlayer(player: ServerPlayer, index: Long) {
        init()
        Utils.giveItemToPlayer(player, itemStack.copy())
    }
}
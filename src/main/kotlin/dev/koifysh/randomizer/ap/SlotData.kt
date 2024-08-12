package dev.koifysh.randomizer.ap

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.mojang.brigadier.StringReader
import dev.koifysh.randomizer.data.items.MinecraftItem
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.ResourceLocationException
import net.minecraft.commands.arguments.item.ItemInput
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class SlotData {
    @SerializedName("include_hard_advancements")
    var include_hard_advancements: Int = 0

    @SerializedName("include_insane_advancements")
    var include_insane_advancements: Int = 0

    @SerializedName("include_postgame_advancements")
    var include_postgame_advancements: Int = 0

    @SerializedName("advancement_goal")
    var advancement_goal: Int = 0

    @SerializedName("mineraft_world_seed")
    var minecraft_world_seed: Long = 0

    @SerializedName("client_version")
    var client_version: Int = 0

    @SerializedName("MC35")
    var MC35: Boolean = false

    @SerializedName("death_link")
    var deathlink: Boolean = false

    @SerializedName("starting_items")
    var startingItems: String? = null

    @Transient
    var startingItemStacks: ArrayList<ItemStack> = ArrayList()

    fun parseStartingItems() {
        val startingItems: JsonArray = JsonParser.parseString(startingItems).getAsJsonArray()
        for (jsonItem: JsonElement in startingItems) {
            val jsonObject: JsonObject = jsonItem.asJsonObject
            val itemName: String = jsonObject.asJsonObject["item"].asString

            val amount = if (jsonObject.has("amount")) jsonObject["amount"].asInt else 1

            try {

                val item = MinecraftItem.itemParser.parse(StringReader(itemName))
                val itemStack = ItemInput(item.item, item.components).createItemStack(amount, false)

                //air is the default item returned if the resource name is invalid.
                if (itemStack.item === Items.AIR) {
                    Utils.sendMessageToAll("No such item \"$itemName\"")
                    continue
                }
                startingItemStacks.add(itemStack)
            } catch (e: Exception) {
                Utils.sendMessageToAll("Error parsing \"$itemName\"")
            }
        }
    }
}

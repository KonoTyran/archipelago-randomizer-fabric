package dev.koifysh.randomizer.ap

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.ResourceLocationException
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
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
    var startingItemStacks: ArrayList<ItemStack> = ArrayList<ItemStack>()

    fun parseStartingItems() {
        val si: JsonArray = JsonParser.parseString(startingItems).getAsJsonArray()
        for (jsonItem in si) {
            val `object` = jsonItem.asJsonObject
            val itemName = `object`.asJsonObject["item"].asString

            val amount = if (`object`.has("amount")) `object`["amount"].asInt else 1

            try {
                val item: Item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName))

                //air is the default item returned if the resource name is invalid.
                if (item === Items.AIR) {
                    Utils.sendMessageToAll("No such item \"$itemName\"")
                    continue
                }

                val iStack: ItemStack = ItemStack(item, amount)

                //todo: figure out starting inventory NBT
//                if(object.has("nbt"))
//                    iStack.set(TagParser.parseTag(object.get("nbt").getAsString()));
                startingItemStacks.add(iStack)

                //            } catch (CommandSyntaxException e) {
//                Utils.sendMessageToAll("NBT error in starting item " + itemName);
            } catch (e: ResourceLocationException) {
                Utils.sendMessageToAll("No such item \"$itemName\"")
            }
        }
    }
}

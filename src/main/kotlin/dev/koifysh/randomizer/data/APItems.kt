package dev.koifysh.randomizer.data

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.structure.ArchipelagoStructures
import dev.koifysh.randomizer.traps.*
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.EnchantedBookItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.LodestoneTracker
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentInstance
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.Structure
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.HashMap

class APItems {

    private var logger: Logger = LoggerFactory.getLogger(ArchipelagoRandomizer.MOD_ID)

    private val itemStacks: HashMap<Long, ItemStack> = object : HashMap<Long, ItemStack>() {
        init {
            val enchantmentRegistry: Registry<Enchantment> = ArchipelagoRandomizer.server.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
            put(45015L, ItemStack(Items.NETHERITE_SCRAP, 8))
            put(45016L, ItemStack(Items.EMERALD, 8))
            put(45017L, ItemStack(Items.EMERALD, 4))

            enchantmentRegistry.getHolder(Enchantments.CHANNELING)
                .ifPresent { put(45018L, EnchantedBookItem.createForEnchantment(EnchantmentInstance(it, 1))) }

            enchantmentRegistry.getHolder(Enchantments.SILK_TOUCH)
                .ifPresent { put(45019L, EnchantedBookItem.createForEnchantment(EnchantmentInstance(it, 1))) }

            enchantmentRegistry.getHolder(Enchantments.SHARPNESS)
                .ifPresent { put(45020L, EnchantedBookItem.createForEnchantment(EnchantmentInstance(it, 3))) }

            enchantmentRegistry.getHolder(Enchantments.PIERCING)
                .ifPresent { put(45021L, EnchantedBookItem.createForEnchantment(EnchantmentInstance(it, 4))) }

            enchantmentRegistry.getHolder(Enchantments.LOOTING)
                .ifPresent { put(45022L, EnchantedBookItem.createForEnchantment(EnchantmentInstance(it, 3))) }

            enchantmentRegistry.getHolder(Enchantments.INFINITY)
                .ifPresent { put(45023L, EnchantedBookItem.createForEnchantment(EnchantmentInstance(it, 1))) }

            put(45024L, ItemStack(Items.DIAMOND_ORE, 4))
            put(45025L, ItemStack(Items.IRON_ORE, 16))
            put(45029L, ItemStack(Items.ENDER_PEARL, 3))
            put(45004L, ItemStack(Items.LAPIS_LAZULI, 4))
            put(45030L, ItemStack(Items.LAPIS_LAZULI, 4))
            put(45031L, ItemStack(Items.COOKED_PORKCHOP, 16))
            put(45032L, ItemStack(Items.GOLD_ORE, 8))
            put(45033L, ItemStack(Items.ROTTEN_FLESH, 8))
            put(45034L, ItemStack(Items.ARROW, 1))
            put(45035L, ItemStack(Items.ARROW, 32))
            put(45036L, ItemStack(Items.SADDLE, 1))

            val compassLore = arrayOf("Right click with compass in hand to", "cycle to next known structure location.")

            val villageCompass: ItemStack = ItemStack(Items.COMPASS, 1)
            makeCompass(villageCompass, ArchipelagoStructures.VILLAGE_TAG)
            addLore(villageCompass, "Structure Compass (Village)", compassLore)
            put(45037L, villageCompass)

            val outpostCompass: ItemStack = ItemStack(Items.COMPASS, 1)
            makeCompass(outpostCompass, ArchipelagoStructures.OUTPOST_TAG)
            addLore(outpostCompass, "Structure Compass (Pillager Outpost)", compassLore)
            put(45038L, outpostCompass)

            val fortressCompass: ItemStack = ItemStack(Items.COMPASS, 1)
            makeCompass(fortressCompass, ArchipelagoStructures.FORTRESS_TAG)
            addLore(fortressCompass, "Structure Compass (Nether Fortress)", compassLore)
            put(45039L, fortressCompass)

            val bastionCompass: ItemStack = ItemStack(Items.COMPASS, 1)
            makeCompass(bastionCompass, ArchipelagoStructures.BASTION_REMNANT_TAG)
            addLore(bastionCompass, "Structure Compass (Bastion Remnant)", compassLore)
            put(45040L, bastionCompass)

            val endCityCompass: ItemStack = ItemStack(Items.COMPASS, 1)
            makeCompass(endCityCompass, ArchipelagoStructures.END_CITY_TAG)
            addLore(endCityCompass, "Structure Compass (End City)", compassLore)
            put(45041L, endCityCompass)

            put(45042L, ItemStack(Items.SHULKER_BOX, 1))
        }
    }

    private val compasses: HashMap<Long, TagKey<Structure>> = object : HashMap<Long, TagKey<Structure>>() {
            init {
                put(45037L, ArchipelagoStructures.VILLAGE_TAG)
                put(45038L, ArchipelagoStructures.OUTPOST_TAG)
                put(45039L, ArchipelagoStructures.FORTRESS_TAG)
                put(45040L, ArchipelagoStructures.BASTION_REMNANT_TAG)
                put(45041L, ArchipelagoStructures.END_CITY_TAG)
            }
        }

    private val xpData: HashMap<Long, Int> = object : HashMap<Long, Int>() {
        init {
            put(45026L, 500)
            put(45027L, 100)
            put(45028L, 50)
        }
    }

    var index: Long = 45100L
    private val trapData: HashMap<Long, Callable<Trap>> = object : HashMap<Long, Callable<Trap>>() {
        init {
            put(index++) { BeeTrap() }
            put(index++) { CreeperTrap() }
            put(index++) { SandRain() }
            put(index++) { FakeWither() }
            put(index++) { GoonTrap() }
            put(index++) { FishFountainTrap() }
            put(index++) { MiningFatigueTrap() }
            put(index++) { BlindnessTrap() }
            put(index++) { PhantomTrap() }
            put(index++) { WaterTrap() }
            put(index++) { GhastTrap() }
            put(index++) { LevitateTrap() }
            put(index++) { AboutFaceTrap() }
            put(index++) { AnvilTrap() }
        }
    }

    private var receivedItems = ArrayList<Long>()

    private val receivedCompasses: ArrayList<TagKey<Structure>> = ArrayList<TagKey<Structure>>()

    private fun makeCompass(iStack: ItemStack, structureTag: TagKey<Structure>) {
        iStack.set<String>(APComponent.TRACKED_STRUCTURE, structureTag.toString())

        iStack.set<MutableComponent>(DataComponents.CUSTOM_NAME, Component.literal("Structure Compass"))

        val structureCords: BlockPos = BlockPos(0, 0, 0)
        iStack.set<LodestoneTracker>(
            DataComponents.LODESTONE_TRACKER,
            LodestoneTracker(
                Optional.of(GlobalPos(Utils.getStructureWorld(structureTag), structureCords)),
                false
            )
        )
    }

    private fun addLore(iStack: ItemStack, name: String, compassLore: Array<String>) {
        iStack.set<MutableComponent>(DataComponents.CUSTOM_NAME, Component.literal(name))

        val itemLore: MutableList<Component> = ArrayList()
        for (s in compassLore) {
            itemLore.add(Component.literal(s))
        }

        iStack.set<ItemLore>(DataComponents.LORE, ItemLore(itemLore))
    }

    fun setReceivedItems(items: ArrayList<Long>) {
        this.receivedItems = items

        items.forEach { id -> compasses[id]?.let { item -> if (!receivedCompasses.contains(item)) receivedCompasses.add(item) }

        ArchipelagoRandomizer.goalManager.updateGoal(false)
    }

    fun giveItem(itemID: Long, player: ServerPlayer) {
        //dont send items to players if game has not started.
        if (ArchipelagoRandomizer.isJailPlayers()) return

        //update the player's index of received items for syncing later.
        ArchipelagoRandomizer.archipelagoWorldData.updatePlayerIndex(player.stringUUID, receivedItems.size)

        if (itemStacks.containsKey(itemID)) {
            val itemStack: ItemStack = itemStacks[itemID]!!.copy()
            if (compasses.containsKey(itemID)) {
                //TODO: figure out CUSTOM_DATA
//                CustomData location = DataComponents
//
//                TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, ResourceLocation.parse());
//                updateCompassLocation(tag, player , itemstack);
            }
            Utils.giveItemToPlayer(player, itemStack)
        } else if (xpData.containsKey(itemID)) {
            player.giveExperiencePoints(xpData[itemID]!!)
        } else if (trapData.containsKey(itemID)) {
            try {
                trapData[itemID]!!.call().trigger(player)
            } catch (ex: Exception) {
                logger.warn("Error triggering trap with id: $itemID: ${ex.message}")
            }
        }
    }


    fun giveItemToAll(itemID: Long) {
        receivedItems.add(itemID)
        //check if this item is a structure compass, and we are not already tracking that one.
        if (compasses.containsKey(itemID) && !receivedCompasses.contains(compasses[itemID])) {
            receivedCompasses.add(compasses[itemID])
        }

        APRandomizer.getServer().execute {
            for (serverplayerentity in APRandomizer.getServer().getPlayerList().getPlayers()) {
                giveItem(itemID, serverplayerentity)
            }
        }

        APRandomizer.getGoalManager().updateGoal(true)
    }

    /***
     * fetches the index form the player's capability then makes sure they have all items after that index.
     * @param player ServerPlayer to catch up
     */
    fun catchUpPlayer(player: ServerPlayer) {
        val playerIndex: Int = APRandomizer.getWorldData().getPlayerIndex(player.getStringUUID())

        for (i in playerIndex until receivedItems.size) {
            giveItem(receivedItems[i], player)
        }
    }

    fun getCompasses(): ArrayList<TagKey<Structure>> {
        return receivedCompasses
    }

    fun getAllItems(): ArrayList<Long> {
        return receivedItems
    }

    companion object {
        // Directly reference a log4j logger.
        private val LOGGER: Logger = LogManager.getLogger()

        const val DRAGON_EGG_SHARD: Long = 45043L

        fun updateCompassLocation(structureTag: TagKey<Structure?>?, player: Player, compass: ItemStack) {
            //get the actual structure data from forge, and make sure its changed to the AP one if needed.

            //get our local custom structure if needed.

            val world: ResourceKey<Level> = Utils.getStructureWorld(structureTag)

            //only locate structure if the player is in the same world as the one for the compass
            //otherwise just point it to 0,0 in said dimension.
            var structurePos: BlockPos? = BlockPos(0, 0, 0)

            var displayName: MutableComponent? = Component.literal(
                java.lang.String.format(
                    "Structure Compass (%s)",
                    Utils.getAPStructureName(structureTag)
                )
            )
            if (player.commandSenderWorld.dimension() == world) {
                try {
                    structurePos = APRandomizer.getServer().getLevel(world)
                        .findNearestMapStructure(structureTag, player.blockPosition(), 75, false)
                } catch (exception: NullPointerException) {
                    player.sendSystemMessage(
                        Component.literal(
                            "Could not find a nearby " + Utils.getAPStructureName(
                                structureTag
                            )
                        )
                    )
                }
            } else {
                displayName = Component.literal(
                    java.lang.String.format(
                        "Structure Compass (%s) Wrong Dimension",
                        Utils.getAPStructureName(structureTag)
                    )
                ).withStyle(ChatFormatting.DARK_RED)
            }

            if (structurePos == null) structurePos = BlockPos(0, 0, 0)


            //update the nbt data with our new structure.

            //nbt.put("structure", StringTag.valueOf(structureTag.location().toString()));
            compass.set<LodestoneTracker>(
                DataComponents.LODESTONE_TRACKER,
                LodestoneTracker(Optional.of<GlobalPos>(GlobalPos(world, structurePos)), false)
            )
            compass.set<MutableComponent>(DataComponents.CUSTOM_NAME, displayName)
        }
    }
}

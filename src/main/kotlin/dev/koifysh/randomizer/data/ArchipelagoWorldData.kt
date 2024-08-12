package dev.koifysh.randomizer.data

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists
import dev.koifysh.randomizer.ArchipelagoRandomizer.apClient
import dev.koifysh.randomizer.ArchipelagoRandomizer.archipelagoWorldData
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.saveddata.SavedData
import org.apache.commons.lang3.ArrayUtils
import java.util.*
import java.util.function.Consumer
import kotlin.collections.HashSet

class ArchipelagoWorldData : SavedData {


    var seedName: String = ""; set(value) { field = value; setDirty() }
    var dragonState: Int = DRAGON_ASLEEP; set(value) { field = value; setDirty() }
    var jailPlayers = true; set(value) { field = value; setDirty() }


    private var locations: MutableSet<Long> = HashSet()

    var index: Long = 0; set(value) { field = value; this.setDirty()}

    private var receivedItems: MutableList<Long> = LinkedList()
    private var playerIndex: MutableMap<String, Int> = HashMap()
    fun addLocation(location: Long) {
        locations.add(location)
        logger.info("Added location $location")
        this.setDirty()
    }

    fun addLocations(locations: Collection<Long>) {
        this.locations.addAll(locations)
        this.setDirty()
        val fileName = "${apClient.roomInfo.seedName}_${apClient.slot}.save"
    }

    fun addItem(id: Long) {
        receivedItems.add(id)
        this.setDirty()
    }

    fun addItem(id: Collection<Long>) {
        receivedItems.addAll(id)
        this.setDirty()
    }

    fun getItems(): ImmutableList<Long> {
        return ImmutableList.copyOf(receivedItems)
    }

    fun getLocations(): Collection<Long> {
        return ImmutableSet.copyOf(locations)
    }

    fun updatePlayerIndex(playerUUID: String, index: Int) {
        playerIndex[playerUUID] = index
        this.setDirty()
    }

    fun getPlayerIndex(playerUUID: String): Int {
        return playerIndex.getOrDefault(playerUUID, 0)
    }


    override fun save(tag: CompoundTag, holder: HolderLookup.Provider): CompoundTag {
        tag.putString("seedName", seedName)
        tag.putInt("dragonState", dragonState)
        tag.putBoolean("jailPlayers", jailPlayers)
        tag.putLongArray("locations", locations.toList())
        tag.putLongArray("items", receivedItems)
        tag.putLong("index", index)
        val tagIndex = CompoundTag()
        playerIndex.forEach { (uuid, index) -> tagIndex.putInt(uuid, index) }
        tag.put("playerIndex", tagIndex)
        return tag
    }

    constructor()

    private constructor(
        seedName: String,
        dragonState: Int,
        jailPlayers: Boolean,
        locations: LongArray,
        items: LongArray,
        playerIndex: MutableMap<String, Int>,
        itemIndex: Long
    ) {
        this.seedName = seedName
        this.dragonState = dragonState
        this.jailPlayers = jailPlayers
        this.locations = locations.toSet().toMutableSet()
        this.receivedItems = items.toMutableList()
        this.playerIndex = playerIndex
        this.index = itemIndex
    }

    companion object {
        const val DRAGON_KILLED: Int = 30
        const val DRAGON_SPAWNED: Int = 20
        const val DRAGON_WAITING: Int = 15
        const val DRAGON_ASLEEP: Int = 10

        fun factory(): Factory<ArchipelagoWorldData> {
            return Factory(
                { ArchipelagoWorldData() },
                { tag: CompoundTag, provider: HolderLookup.Provider? -> load(tag, provider) },
                null
            )
        }

        fun load(tag: CompoundTag, provider: HolderLookup.Provider?): ArchipelagoWorldData {
            val indexTag = tag.getCompound("playerIndex")
            val indexMap = HashMap<String, Int>()
            indexTag.allKeys.forEach(Consumer { key: String -> indexMap[key] = indexTag.getInt(key) })
            return ArchipelagoWorldData(
                tag.getString("seedName"),
                tag.getInt("dragonState"),
                tag.getBoolean("jailPlayers"),
                tag.getLongArray("locations"),
                tag.getLongArray("items"),
                indexMap,
                tag.getLong("index")
            )
        }
    }
}

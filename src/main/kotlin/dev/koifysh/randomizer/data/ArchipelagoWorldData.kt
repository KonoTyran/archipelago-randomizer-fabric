package dev.koifysh.randomizer.data

import com.google.common.collect.Lists
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.saveddata.SavedData
import org.apache.commons.lang3.ArrayUtils
import java.util.*
import java.util.function.Consumer

class ArchipelagoWorldData : SavedData {

    var seedName: String = ""; set(value) { field = value; setDirty() }
    var dragonState: Int = DRAGON_ASLEEP; set(value) { field = value; setDirty() }
    var jailPlayers = false; set(value) { field = value; setDirty() }

    private var locations: MutableSet<Long> = HashSet()
    private var index: Long = 0
    private var playerIndex: MutableMap<String, Int> = HashMap()

    fun addLocation(location: Long) {
        locations.add(location)
        this.setDirty()
    }

    fun addLocations(locations: Array<Long>?) {
        this.locations.addAll(Lists.newArrayList(Arrays.stream(locations).iterator()))
        this.setDirty()
    }

    fun getLocations(): Set<Long> {
        return locations
    }

    fun updatePlayerIndex(playerUUID: String, index: Int) {
        playerIndex[playerUUID] = index
        this.setDirty()
    }

    fun getPlayerIndex(playerUUID: String): Int {
        return playerIndex.getOrDefault(playerUUID, 0)
    }

    var itemIndex: Long
        get() = this.index
        set(index) {
            this.index = index
            this.setDirty()
        }

    override fun save(tag: CompoundTag, holder: HolderLookup.Provider): CompoundTag {
        tag.putString("seedName", seedName)
        tag.putInt("dragonState", dragonState)
        tag.putBoolean("jailPlayers", jailPlayers)
        tag.putLongArray("locations", locations.stream().toList())
        tag.putLong("index", index)
        val tagIndex = CompoundTag()
        playerIndex.forEach { (string: String, i: Int) -> tagIndex.putInt(string, i) }
        tag.put("playerIndex", tagIndex)
        return tag
    }

    constructor()

    private constructor(
        seedName: String,
        dragonState: Int,
        jailPlayers: Boolean,
        locations: LongArray,
        playerIndex: Map<String, Int>,
        itemIndex: Long
    ) {
        this.seedName = seedName
        this.dragonState = dragonState
        this.jailPlayers = jailPlayers
        this.locations = HashSet(mutableSetOf(*ArrayUtils.toObject(locations)))
        this.playerIndex = HashMap()
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
                indexMap,
                tag.getLong("index")
            )
        }
    }
}

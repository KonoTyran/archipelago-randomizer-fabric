package dev.koifysh.randomizer.data

import com.google.gson.annotations.SerializedName

class APMCData {
    @SerializedName("world_seed")
    var worldSeed: Long = 0

    @SerializedName("structures")
    var structures: HashMap<String, String> = HashMap()

    @SerializedName("seed_name")
    var seedName: String = ""

    @SerializedName("player_name")
    var playerName: String = ""

    @SerializedName("player_id")
    var playerID: Int = 0

    @SerializedName("client_version")
    var clientVersion: Int = 0

    @SerializedName("race")
    var race: Boolean = false

    @SerializedName("egg_shards_required")
    var eggShardsRequired: Int = -1

    @SerializedName("egg_shards_available")
    var eggShardsAvailable: Int = -1

    @SerializedName("advancement_goal")
    var advancementsRequired: Int = -1

    @SerializedName("required_bosses")
    var requiredBosses: Bosses = Bosses.ENDER_DRAGON

    @SerializedName("server")
    var server: String = ""

    @SerializedName("port")
    var port: Int = 0

    var state: State = State.VALID

    var advancements: HashMap<String, Long> = HashMap()
    var hardAdvancements: Set<Long> = HashSet()
    var veryHardAdvancements: Set<Long> = HashSet()

    enum class State {
        VALID, MISSING, INVALID_VERSION, INVALID_SEED
    }

    enum class Bosses {
        @SerializedName("none")
        NONE,
        @SerializedName("ender_dragon")
        ENDER_DRAGON,
        @SerializedName("wither")
        WITHER,
        @SerializedName("both")
        BOTH
    }
}
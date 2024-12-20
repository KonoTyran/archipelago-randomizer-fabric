package dev.koifysh.randomizer.data

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.registries.APGoal
import dev.koifysh.randomizer.registries.APItem
import dev.koifysh.randomizer.registries.APLocation
import java.nio.file.Path

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

    @SerializedName("locations")
    var apLocations: List<APLocation> = ArrayList()

    @SerializedName("items")
    var apItems: List<APItem> = ArrayList()

    @SerializedName("goals")
    var apGoals: MutableList<APGoal> = ArrayList()

    var state: State = State.VALID

    var fileName: Path? = null

    enum class State {
        VALID, MISSING, INVALID_VERSION, INVALID_SEED
    }

    enum class Bosses {
        @SerializedName("none", alternate = ["0"])
        NONE,
        @SerializedName("ender_dragon", alternate = ["1"])
        ENDER_DRAGON,
        @SerializedName("wither", alternate = ["2"])
        WITHER,
        @SerializedName("both", alternate = ["3"])
        BOTH
    }
}

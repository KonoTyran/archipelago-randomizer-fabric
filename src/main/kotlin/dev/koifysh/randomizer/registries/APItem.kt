package dev.koifysh.randomizer.registries

import com.google.gson.annotations.SerializedName

data class APItem (
    @SerializedName("id")
    var id: Long = 0,

    @SerializedName("rewards")
    var rewards: ArrayList<APItemReward> = ArrayList(),
)

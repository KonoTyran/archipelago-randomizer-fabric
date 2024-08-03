package dev.koifysh.randomizer.registries

import com.google.gson.annotations.SerializedName

abstract class APItem {

    @SerializedName("id")
    var id: Long = 0

    @SerializedName("type")
    var type: String = ""

    abstract fun grant()

}

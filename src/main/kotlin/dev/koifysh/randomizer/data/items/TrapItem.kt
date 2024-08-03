package dev.koifysh.randomizer.data.items

import com.google.gson.annotations.SerializedName
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.registries.APItem
import dev.koifysh.randomizer.traps.Trap
import dev.koifysh.randomizer.traps.Traps

class TrapItem: APItem() {

    @SerializedName("trap_name")
    var trap: String = "empty:empty"

    override fun grant() {
        return Traps.trigger(trap)
    }

}
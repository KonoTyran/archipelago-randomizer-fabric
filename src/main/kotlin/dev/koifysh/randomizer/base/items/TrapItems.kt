package dev.koifysh.randomizer.base.items

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.base.items.traps.*
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.Callable

object TrapItems {

    private val trapData = HashMap<ResourceLocation, Callable<Trap>>()

    fun trigger(trapLocation: ResourceLocation) {
        try {
            val trap = trapData[trapLocation]?.call() ?: return
            ArchipelagoRandomizer.server.playerList.players.forEach {
                trap.trigger(it)
            }
        } catch (e: Exception) {
            ArchipelagoRandomizer.logger.error("Failed to trigger trap: $trapLocation - ${e.message}")
        }
    }

    fun init() {
        trapData.clear()
        trapData[ArchipelagoRandomizer.modResource("trap/bee")] = Callable { BeeTrap() }
        trapData[ArchipelagoRandomizer.modResource("trap/creeper")] = Callable { CreeperTrap() }
        trapData[ArchipelagoRandomizer.modResource("trap/sand_rain")] = Callable { SandRain() }
        trapData[ArchipelagoRandomizer.modResource("trap/fake_wither")] = Callable { FakeWither() }
        trapData[ArchipelagoRandomizer.modResource("trap/goon")] = Callable { GoonTrap() }
        trapData[ArchipelagoRandomizer.modResource("trap/fish_fountain")] = Callable { FishFountainTrap() }
        trapData[ArchipelagoRandomizer.modResource("trap/mining_fatigue")] = Callable { MiningFatigueTrap() }
        trapData[ArchipelagoRandomizer.modResource("trap/blindness")] = Callable { BlindnessTrap() }
        trapData[ArchipelagoRandomizer.modResource("trap/phantom")] = Callable { PhantomTrap() }
        trapData[ArchipelagoRandomizer.modResource("trap/water")] = Callable { WaterTrap() }
        trapData[ArchipelagoRandomizer.modResource("trap/ghast")] = Callable { GhastTrap() }
        trapData[ArchipelagoRandomizer.modResource("trap/levitate")] = Callable { LevitateTrap() }
        trapData[ArchipelagoRandomizer.modResource("trap/about_face")] = Callable { AboutFaceTrap() }
        trapData[ArchipelagoRandomizer.modResource("trap/anvil")] = Callable { AnvilTrap() }
    }

}
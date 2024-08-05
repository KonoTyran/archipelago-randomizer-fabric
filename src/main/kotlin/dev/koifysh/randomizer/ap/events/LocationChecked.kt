package dev.koifysh.randomizer.ap.events

import dev.koifysh.archipelago.events.ArchipelagoEventListener
import dev.koifysh.archipelago.events.CheckedLocationsEvent

object LocationChecked {
    @ArchipelagoEventListener
    fun onLocationChecked(event: CheckedLocationsEvent?) {
//        event.checkedLocations.forEach(location -> APRandomizer.getAdvancementManager().addAdvancement(location));
    }
}

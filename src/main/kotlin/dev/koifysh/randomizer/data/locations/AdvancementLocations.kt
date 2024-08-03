package dev.koifysh.randomizer.data.locations

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.advancementLocations
import dev.koifysh.randomizer.ArchipelagoRandomizer.server
import dev.koifysh.randomizer.registries.APLocation
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementProgress
import net.minecraft.advancements.DisplayInfo
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer

class AdvancementLocations {

    private val advancements: HashMap<ResourceLocation, Long> = HashMap()
    private val earnedAdvancements: HashSet<Long> = HashSet()

    private fun getAdvancementID(advancement: ResourceLocation): Long {
        advancements[advancement]?.let { return it }
        return 0L
    }

    private fun hasAdvancement(namespacedID: ResourceLocation): Boolean {
        return earnedAdvancements.contains(getAdvancementID(namespacedID))
    }

    fun syncAllAdvancements() {
        for (a in server.advancements.allAdvancements) {
            syncAdvancement(a)
        }
    }

    private fun syncProgress(advancementHolder: AdvancementHolder, progress: String) {
        for (serverPlayerEntity in server.playerList.players) {
            val ap = serverPlayerEntity.advancements.getOrStartProgress(advancementHolder)
            if (ap.isDone) continue
            if (ap.remainingCriteria.contains(progress)) {
                serverPlayerEntity.advancements.award(advancementHolder, progress)
            }
        }
    }

    fun syncAdvancement(advancementHolder: AdvancementHolder) {
        for (serverPlayerEntity in server.playerList.players) {
            val ap = serverPlayerEntity.advancements.getOrStartProgress(advancementHolder)
            if (ap.isDone) continue
            for (remainingCriterion in ap.remainingCriteria) {
                serverPlayerEntity.advancements.award(advancementHolder, remainingCriterion)
            }
        }
    }

    fun onAdvancementGrant(holder: AdvancementHolder, player: ServerPlayer) {
        val id = getAdvancementID(holder.id)

        // don't send the same advancement twice
        if (earnedAdvancements.contains(id)) return

        earnedAdvancements.add(id)
        ArchipelagoRandomizer.locationRegister.sendLocation(id)

        holder.value().display().ifPresent { displayInfo: DisplayInfo ->
            if (displayInfo.shouldAnnounceChat()) {
                server.playerList.broadcastSystemMessage(
                    displayInfo.type.createAnnouncement(holder, player),
                    false
                )
            }
        }
    }

    fun addLocation(apLocation: APLocation) {
        val location = apLocation as Advancement
        if (advancements.containsValue(location.id)) {
            ArchipelagoRandomizer.logger.error("duplicate location ID detected! duplicate entry {}: \"{}\"", location.id, location.advancement)
            return
        }
        advancements[location.advancement] = location.id
        ArchipelagoRandomizer.logger.trace("Registering Advancement {}", location.advancement)
    }

    fun onAdvancementProgress(advancementHolder: AdvancementHolder, advancementProgress: AdvancementProgress) {
//        if (!advancements.containsKey(advancementHolder.id())) return

        advancementProgress.completedCriteria.forEach(Consumer { criterion: String ->
            advancementLocations.syncProgress(
                advancementHolder,
                criterion
            )
        })
    }

    fun isTracked(id: ResourceLocation): Boolean {
        return advancements.containsKey(id)
    }

}
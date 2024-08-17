package dev.koifysh.randomizer.base.locations

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.advancementLocations
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
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

    private fun getAdvancementID(advancement: ResourceLocation): Long {
        advancements[advancement]?.let { return it }
        return 0L
    }

    private fun isAdvancementDone(namespacedID: ResourceLocation): Boolean {
        return ArchipelagoRandomizer.archipelagoWorldData.getCompletedLocations().contains(getAdvancementID(namespacedID))
    }

    fun grantCompletedAdvancements(player: ServerPlayer) {
        val completed = server.advancements.allAdvancements.filter { isAdvancementDone(it.id) }
        completed.forEach { advancement ->
            player.advancements.getOrStartProgress(advancement).remainingCriteria.forEach { criterion ->
                player.advancements.award(advancement, criterion)
            }
        }
    }

    fun grantCompletedAdvancementsToAll() {
        val completed = server.advancements.allAdvancements.filter { isAdvancementDone(it.id) }
        server.playerList.players.forEach { player ->
            completed.forEach { advancement ->
                player.advancements.getOrStartProgress(advancement).remainingCriteria.forEach { criterion ->
                    player.advancements.award(advancement, criterion)
                }
            }
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
        // don't send the same advancement twice
        if (isAdvancementDone(holder.id)) return
        // don't send advancements that are not tracked
        if (!isTracked(holder.id)) return

        val id = getAdvancementID(holder.id)
        logger.info("Player ${player.name} has completed advancement ${holder.id} with ID $id")
        ArchipelagoRandomizer.archipelagoWorldData.addLocation(id)
        ArchipelagoRandomizer.locationRegister.sendLocation(id)

        holder.value().display().ifPresent { displayInfo: DisplayInfo ->
            server.playerList.broadcastSystemMessage(
                displayInfo.type.createAnnouncement(holder, player),
                false
            )
        }
    }

    fun addLocation(apLocation: APLocation) {
        val location = apLocation as Advancement
        if (advancements.containsValue(location.id)) {
            logger.error(
                "duplicate location ID detected! duplicate entry {}: \"{}\"",
                location.id,
                location.advancement
            )
            return
        }
        advancements[location.advancement] = location.id
        logger.trace("Registering Advancement {}", location.advancement)
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
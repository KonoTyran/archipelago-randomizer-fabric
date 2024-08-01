package dev.koifysh.randomizer.data

import dev.koifysh.randomizer.ArchipelagoRandomizer
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.resources.ResourceLocation

class APLocations constructor(apmcData: APMCData) {
    private val advancements: HashMap<ResourceLocation, Long> = HashMap()
    private var hardAdvancements: HashSet<Long> = HashSet()
    private var veryHardAdvancements: HashSet<Long> = HashSet()
    private val earnedAdvancements: HashSet<Long> = HashSet()

    init {
        apmcData.advancements.forEach { (advancement, id) ->
            advancements[ResourceLocation.parse(advancement)] = id
        }
        hardAdvancements.addAll(apmcData.hardAdvancements)
        veryHardAdvancements.addAll(apmcData.veryHardAdvancements)
    }

    fun getAdvancementID(advancement: ResourceLocation): Long {
        advancements[advancement]?.let { return it }
        return 0L
    }

    fun hasAdvancement(id: Long): Boolean {
        return earnedAdvancements.contains(id)
    }

    fun hasAdvancement(namespacedID: ResourceLocation): Boolean {
        return earnedAdvancements.contains(getAdvancementID(namespacedID))
    }

    fun setCheckedAdvancements(checkedLocations: Set<Long>) {
        earnedAdvancements.addAll(checkedLocations)
        val data = ArchipelagoRandomizer.archipelagoWorldData
        for (checkedLocation in checkedLocations) {
            ArchipelagoRandomizer.archipelagoWorldData
            data.addLocation(checkedLocation)
        }

        syncAllAdvancements()
    }

    fun syncAllAdvancements() {
        for (a in ArchipelagoRandomizer.server.advancements.allAdvancements) {
            syncAdvancement(a)
        }
    }

    fun syncAdvancement(a: AdvancementHolder) {
        if (hasAdvancement(a.id())) {
            for (serverPlayerEntity in ArchipelagoRandomizer.server.playerList.players) {
                val ap = serverPlayerEntity.advancements.getOrStartProgress(a)
                if (ap.isDone) continue
                for (remainingCriterion in ap.remainingCriteria) {
                    serverPlayerEntity.advancements.award(a, remainingCriterion)
                }
            }
        }
    }
}
package dev.koifysh.randomizer.ktmixin

import com.google.common.collect.ImmutableMap
import dev.koifysh.randomizer.ArchipelagoRandomizer.advancementLocations
import net.minecraft.ChatFormatting
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.DisplayInfo
import net.minecraft.resources.ResourceLocation
import java.util.*

object KTMixinAdvancementLoad {

    fun onAdvancementLoad(old: ImmutableMap.Builder<ResourceLocation, AdvancementHolder>): ImmutableMap<ResourceLocation, AdvancementHolder> {
        val newAdvancements = TreeMap<ResourceLocation, AdvancementHolder>()
        for ((resourceLocation, advancementHolder) in old.buildOrThrow().entries) {
            val oldAdvancement = advancementHolder.value()
            val newAdvancement = Advancement(
                oldAdvancement.parent(),
                getDisplayInfo(advancementHolder),
                oldAdvancement.rewards(),
                oldAdvancement.criteria(),
                oldAdvancement.requirements(),
                oldAdvancement.sendsTelemetryEvent(),
                oldAdvancement.name()
            )


            newAdvancements[resourceLocation] = AdvancementHolder(resourceLocation, newAdvancement)
        }

        newAdvancements.toSortedMap{ o1, o2 -> o1.namespace.compareTo(o2.namespace) }

        return ImmutableMap.copyOf(newAdvancements)
    }

    private fun getDisplayInfo(holder: AdvancementHolder): Optional<DisplayInfo> {
        var newDisplay: Optional<DisplayInfo> = Optional.empty()
        val advancement = holder.value()
        if (advancement.display().isPresent) {
            val oldDisplay = advancement.display().get()
            val newTitle = oldDisplay.title.copy()
            if (!advancementLocations.isTracked(holder.id())) {
                if (!holder.id().path.startsWith("received/")) newTitle.withStyle(ChatFormatting.STRIKETHROUGH)
            }
            newDisplay = Optional.of(
                DisplayInfo(
                    oldDisplay.icon,
                    newTitle,
                    oldDisplay.description,
                    oldDisplay.background,
                    oldDisplay.type,
                    oldDisplay.shouldShowToast(),
                    oldDisplay.shouldAnnounceChat(),
                    false // Do not hide the advancement
                )
            )
        }
        return newDisplay
    }
}
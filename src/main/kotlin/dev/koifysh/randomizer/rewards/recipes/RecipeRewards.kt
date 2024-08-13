package dev.koifysh.randomizer.rewards.recipes

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.ArchipelagoRandomizer.logger
import dev.koifysh.randomizer.ArchipelagoRandomizer.server
import dev.koifysh.randomizer.registries.APItemReward
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.crafting.RecipeHolder

class RecipeRewards {

    private val recipes: ArrayList<RecipeHolder<*>> = ArrayList()
    private val allRewards = ArrayList<APRecipe>()

    val restrictedRecipes: HashSet<RecipeHolder<*>> = HashSet()
    private val trackingAdvancements: HashSet<ResourceLocation> = HashSet()

    fun registerRecipe(reward: APItemReward) {
        if (reward !is APRecipe) return

        allRewards.add(reward)
    }

    fun initialize() {
        logger.info("Initializing ${allRewards.size} rewards")
        recipes.clear()
        restrictedRecipes.clear()

        allRewards.forEach { reward ->
            val allRecipe = reward.getAllRecipes()
            recipes.addAll(allRecipe)
            restrictedRecipes.addAll(allRecipe)
        }
        logger.info("Initialized ${recipes.size} recipes")

        ArchipelagoRandomizer.itemRewardRegister.getReceivedItems().forEachIndexed { index, item ->
            item.rewards.forEach reward@{ reward ->
                if (reward !is APRecipe) return@reward
                reward.onItemObtain(index.toLong())
            }
        }
        syncAllTrackingAdvancements()
    }

    fun track(advancements: Set<ResourceLocation>) {
        trackingAdvancements.addAll(advancements)
        syncAllTrackingAdvancements()
    }

    fun syncTrackingAdvancements(player: ServerPlayer) {
        val holders = HashSet<AdvancementHolder>()
        trackingAdvancements.forEach { advancementRL ->
            server.advancements.get(advancementRL)?.let { holders.add(it) }
        }
        holders.forEach { holder ->
            player.advancements.getOrStartProgress(holder).remainingCriteria.forEach { criteria ->
                player.advancements.award(holder, criteria)
            }
        }
    }

    fun syncAllTrackingAdvancements() {
        val holders = HashSet<AdvancementHolder>()
        trackingAdvancements.forEach { advancementRL ->
            server.advancements.get(advancementRL)?.let { holders.add(it) }
        }
        //give every player on the server the advancements
        server.playerList.players.forEach { player ->
            holders.forEach { holder ->
                player.advancements.getOrStartProgress(holder).remainingCriteria.forEach { criteria ->
                    player.advancements.award(holder, criteria)
                }
            }
        }
    }

    fun add(toGrant: Set<RecipeHolder<*>>) {
        restrictedRecipes.removeAll(toGrant)

    }

}
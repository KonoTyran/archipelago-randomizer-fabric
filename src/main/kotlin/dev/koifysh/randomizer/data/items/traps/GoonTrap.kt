package dev.koifysh.randomizer.data.items.traps

import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.Utils
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments

class GoonTrap @JvmOverloads constructor(numberOfGoons: Int = 3) : Trap {
    private val numberOfGoons: Int
    private var zombies: MutableList<Zombie> = ArrayList()

    private var timer: Int = 20 * 30

    init {
        ServerTickEvents.END_SERVER_TICK.register { onTick() }
        this.numberOfGoons = numberOfGoons
    }

    override fun trigger(player: ServerPlayer) {
        val fish = ItemStack(Items.SALMON)
        val enchantmentRegistry = ArchipelagoRandomizer.server.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
        fish.enchant(enchantmentRegistry.getHolder(Enchantments.KNOCKBACK).get(), 3)

        ArchipelagoRandomizer.server.execute {
            val world: ServerLevel = player.level() as ServerLevel
            for (i in 0 until numberOfGoons) {
                val goon: Zombie = EntityType.ZOMBIE.create(world) ?: continue
                goon.setItemInHand(InteractionHand.MAIN_HAND, fish.copy())
                goon.target = player

                goon.moveTo(Utils.getRandomPosition(player.position(), 5))
                zombies.add(goon)
                world.addFreshEntity(goon)
            }
        }
    }

    private fun onTick() {
        if (--timer != 0) return

        for (zombie in zombies) {
            zombie.kill()
        }
    }
}
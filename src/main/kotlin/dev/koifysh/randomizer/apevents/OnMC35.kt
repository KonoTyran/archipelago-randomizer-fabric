package dev.koifysh.randomizer.apevents

import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.koifysh.archipelago.events.ArchipelagoEventListener
import dev.koifysh.archipelago.events.BouncedEvent
import dev.koifysh.randomizer.ArchipelagoRandomizer
import dev.koifysh.randomizer.utils.Utils
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.TagParser
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import java.util.concurrent.ThreadLocalRandom

object OnMC35 {
    @ArchipelagoEventListener
    fun onBounced(event: BouncedEvent) {
        if (!event.tags.contains("MC35") && !ArchipelagoRandomizer.apClient.slotData.MC35) return

        val sourceSlot: Int = event.getInt("source")
        if (sourceSlot != ArchipelagoRandomizer.apClient.slot) {
            val randPlayer = ThreadLocalRandom.current().nextInt(ArchipelagoRandomizer.server.playerCount)
            val player: ServerPlayer = ArchipelagoRandomizer.server.playerList.players[randPlayer]
            var eNBT = CompoundTag()
            try {
                if (event.containsKey("nbt")) eNBT = TagParser.parseTag(event.getString("nbt"))
            } catch (ignored: CommandSyntaxException) {
            }
            eNBT.putString("id", event.getString("enemy"))
            val entity: Entity? =
                EntityType.loadEntityRecursive(eNBT, player.level()) { spawnEntity: Entity ->
                    val pos: Vec3 = player.position()
                    val offset: Vec3 = Utils.getRandomPosition(pos, 10)
                    spawnEntity.moveTo(offset.x, offset.y, offset.z, spawnEntity.yRotO, spawnEntity.xRotO)
                    spawnEntity
                }
            if (entity != null) {
                if (entity is LivingEntity) {
                    entity.heal(entity.maxHealth)
                    entity.setLastHurtByPlayer(player)
                }
                player.level().addFreshEntity(entity)
            }
        }
    }
}

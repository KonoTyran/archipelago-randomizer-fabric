package dev.koifysh.randomizer.utils

import dev.koifysh.randomizer.ArchipelagoRandomizer

import net.minecraft.server.MinecraftServer
import java.util.*

object TitleQueue {

    private var titleQueue: MutableList<QueuedTitle> = LinkedList<QueuedTitle>()

    private var titleTime: Int = 0

    fun onServerTick(server: MinecraftServer) {
        if (titleQueue.isNotEmpty()) {
            if (titleTime <= 0) {
                val title = titleQueue.first()
                titleQueue.removeFirst()
                titleTime = title.ticks
                title.sendTitle()
            }
        }
        if (titleTime > 0) {
            titleTime -= 1
        }
    }


    fun queueTitle(queuedTitle: QueuedTitle) {
        titleQueue.add(queuedTitle)
    }

    fun ClearQueue() {
        titleQueue.clear()
    }
}

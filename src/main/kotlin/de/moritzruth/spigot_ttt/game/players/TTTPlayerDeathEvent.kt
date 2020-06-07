package de.moritzruth.spigot_ttt.game.players

import org.bukkit.Location
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TTTPlayerDeathEvent(
    val tttPlayer: TTTPlayer,
    val location: Location
): Event() {
    override fun getHandlers(): HandlerList {
        @Suppress("RedundantCompanionReference") // false positive
        return Companion.handlers
    }

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList() = handlers
    }
}

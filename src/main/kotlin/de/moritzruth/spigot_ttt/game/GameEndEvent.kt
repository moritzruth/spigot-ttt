package de.moritzruth.spigot_ttt.game

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GameEndEvent(val aborted: Boolean): Event() {
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

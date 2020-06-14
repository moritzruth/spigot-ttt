package de.moritzruth.spigot_ttt.game.players

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TTTPlayerDamageEvent(
    val tttPlayer: TTTPlayer,
    var damage: Double,
    val deathReason: DeathReason
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

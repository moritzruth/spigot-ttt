package de.moritzruth.spigot_ttt.game.corpses

import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

data class CorpseClickEvent(
    val tttPlayer: TTTPlayer,
    val tttCorpse: TTTCorpse
): Event(), Cancellable {
    private var _cancelled = false

    override fun getHandlers(): HandlerList {
        @Suppress("RedundantCompanionReference") // false positive
        return Companion.handlers
    }

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList() = handlers
    }

    override fun isCancelled(): Boolean = _cancelled
    override fun setCancelled(cancel: Boolean) = run { _cancelled = cancel }
}

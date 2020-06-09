package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.game.corpses.TTTCorpse
import org.bukkit.Location
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

data class TTTPlayerDeathEvent(
    val tttPlayer: TTTPlayer,
    val location: Location,
    val tttCorpse: TTTCorpse,
    val killer: TTTPlayer?,
    var scream: Boolean = true
): Event() {
    override fun getHandlers(): HandlerList {
        @Suppress("RedundantCompanionReference") // false positive
        return Companion.handlers
    }

    var letRoundEnd = true

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList() = handlers
    }
}

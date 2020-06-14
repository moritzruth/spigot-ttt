package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.game.corpses.TTTCorpse
import org.bukkit.Location
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class TTTPlayerDeathEvent(
    val tttPlayer: TTTPlayer,
    val location: Location,
    val killer: TTTPlayer?,
    var scream: Boolean = true
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

class TTTPlayerTrueDeathEvent(
    tttPlayer: TTTPlayer,
    location: Location,
    killer: TTTPlayer?,
    scream: Boolean = true,
    val tttCorpse: TTTCorpse,
    var winnerRoleGroup: RoleGroup? = null
): TTTPlayerDeathEvent(
    tttPlayer,
    location,
    killer,
    scream
) {
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

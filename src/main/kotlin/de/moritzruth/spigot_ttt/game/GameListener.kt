package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerEvent

abstract class GameListener: Listener {
    protected fun handle(event: InventoryClickEvent, handler: (tttPlayer: TTTPlayer) -> Unit) {
        val whoClicked = event.whoClicked
        if (whoClicked is Player) {
            handler(TTTPlayer.of(whoClicked) ?: return)
        }
    }

    protected open fun handle(event: InventoryCloseEvent, handler: (tttPlayer: TTTPlayer) -> Unit) {
        val player = event.player
        if (player is Player) {
            handler(TTTPlayer.of(player) ?: return)
        }
    }

    protected fun <T: PlayerEvent> handle(event: T, handler: (tttPlayer: TTTPlayer) -> Unit) {
        handler(TTTPlayer.of(event.player) ?: return)
    }
}

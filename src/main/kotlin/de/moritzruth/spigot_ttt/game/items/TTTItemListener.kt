package de.moritzruth.spigot_ttt.game.items

import de.moritzruth.spigot_ttt.game.GameListener
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayerReviveEvent
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerItemConsumeEvent

open class TTTItemListener<InstanceT: TTTItem.Instance>(private val tttItem: TTTItem<InstanceT>): GameListener() {
    protected fun handle(
        event: EntityDamageByEntityEvent,
        handler: (damagerTTTPlayer: TTTPlayer, damagedTTTPlayer: TTTPlayer) -> Unit
    ) {
        val damager = event.damager
        val damaged = event.entity

        if (
            damager is Player &&
            damaged is Player &&
            damager.inventory.itemInMainHand.type == tttItem.material
        ) {
            val damagerTTTPlayer = TTTPlayer.of(damager) ?: return
            val damagedTTTPlayer = TTTPlayer.of(damaged) ?: return
            handler(damagerTTTPlayer, damagedTTTPlayer)
        }
    }

    protected fun handle(event: PlayerItemConsumeEvent, handler: (tttPlayer: TTTPlayer) -> Unit) {
        if (event.item.type == tttItem.material) {
            handler(TTTPlayer.of(event.player) ?: return)
        }
    }

    protected fun handleWithInstance(event: InventoryCloseEvent, handler: (instance: InstanceT) -> Unit) {
        val player = event.player
        if (player is Player) handler(TTTPlayer.of(player)?.let { tttItem.getInstance(it) } ?: return)
    }

    protected fun handleWithInstance(event: InventoryClickEvent, handler: (instance: InstanceT) -> Unit) {
        val whoClicked = event.whoClicked
        if (whoClicked is Player) {
            val tttPlayer = TTTPlayer.of(whoClicked) ?: return
            val instance = tttItem.getInstance(tttPlayer) ?: return
            handler(instance)
        }
    }

    protected fun handleWithInstance(event: PlayerEvent, handler: (instance: InstanceT) -> Unit) {
        val player = event.player
        val tttPlayer = TTTPlayer.of(player) ?: return
        val instance = tttItem.getInstance(tttPlayer) ?: return
        handler(instance)
    }

    protected fun handle(event: TTTPlayerReviveEvent, handler: (instance: InstanceT) -> Unit) {
        handler(tttItem.getInstance(event.tttPlayer) ?: return)
    }
}

package de.moritzruth.spigot_ttt

import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.utils.isLeftClick
import de.moritzruth.spigot_ttt.utils.isRightClick
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack

open class TTTItemListener(private val tttItem: TTTItem, private val cancelDamage: Boolean): Listener {
    @EventHandler
    open fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) = handle(event) { _, _ ->
        if (cancelDamage) event.isCancelled = true
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) = handle(event) { tttPlayer ->
        event.isCancelled = true
        val data = ClickEventData(tttPlayer, event.item!!, event)
        if (event.action.isRightClick) onRightClick(data)
        else if (event.action.isLeftClick) onLeftClick(data)
    }

    open fun onRightClick(data: ClickEventData) {}
    open fun onLeftClick(data: ClickEventData) {}

    protected fun handle(event: PlayerInteractEvent, handler: (tttPlayer: TTTPlayer) -> Unit) {
        if (event.item?.type == tttItem.itemStack.type) {
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return
            handler(tttPlayer)
        }
    }

    protected fun handle(event: PlayerSwapHandItemsEvent, handler: (tttPlayer: TTTPlayer) -> Unit) {
        if (event.offHandItem?.type == tttItem.itemStack.type) {
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return
            handler(tttPlayer)
        }
    }

    protected fun handle(
        event: EntityDamageByEntityEvent,
        handler: (damagerTTTPlayer: TTTPlayer, damagedTTTPlayer: TTTPlayer) -> Unit
    ) {
        val damager = event.damager
        val damaged = event.entity

        if (
            damager is Player &&
            damaged is Player &&
            damager.inventory.itemInMainHand.type == tttItem.itemStack.type
        ) {
            val damagerTTTPlayer = PlayerManager.getTTTPlayer(damager) ?: return
            val damagedTTTPlayer = PlayerManager.getTTTPlayer(damaged) ?: return
            handler(damagerTTTPlayer, damagedTTTPlayer)
        }
    }

    protected fun handle(event: InventoryClickEvent, handler: (tttPlayer: TTTPlayer) -> Unit) {
        val whoClicked = event.whoClicked
        if (whoClicked is Player) {
            handler(PlayerManager.getTTTPlayer(whoClicked) ?: return)
        }
    }

    protected fun handle(event: InventoryCloseEvent, handler: (tttPlayer: TTTPlayer) -> Unit) {
        val player = event.player
        if (player is Player) {
            handler(PlayerManager.getTTTPlayer(player) ?: return)
        }
    }

    protected fun <T: PlayerEvent> handle(event: T, handler: (tttPlayer: TTTPlayer) -> Unit) {
        handler(PlayerManager.getTTTPlayer(event.player) ?: return)
    }

    protected fun handle(event: PlayerItemConsumeEvent, handler: (tttPlayer: TTTPlayer) -> Unit) {
        if (event.item.type == tttItem.itemStack.type) {
            handler(PlayerManager.getTTTPlayer(event.player) ?: return)
        }
    }

    data class ClickEventData(
        val tttPlayer: TTTPlayer,
        val itemStack: ItemStack,
        val event: PlayerInteractEvent
    )
}

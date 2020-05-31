package de.moritzruth.spigot_ttt.items.weapons.guns

import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.utils.noop
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class GunListener(private val gun: Gun<*>): Listener {

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.isRelevant(gun.displayName)) event.isCancelled = true
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!event.isRelevant(gun.displayName)) return
        val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

        try {
            when(event.action) {
                Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> gun.reload(tttPlayer, event.item!!)
                Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> gun.shoot(tttPlayer, event.item!!)
                else -> noop()
            }
        } catch (e: Gun.ActionInProgressError) {}
    }
}

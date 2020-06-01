package de.moritzruth.spigot_ttt.items.cloaking_device

import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.items.isRelevant
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSprintEvent

object CloakingDeviceListener: Listener {
    @EventHandler
    fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) {
        val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

        if (event.isSprinting && CloakingDevice.getState(tttPlayer).enabled) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!event.isRelevant(CloakingDevice.displayName)) return
        val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

        CloakingDevice.setEnabled(tttPlayer, null)
    }
}

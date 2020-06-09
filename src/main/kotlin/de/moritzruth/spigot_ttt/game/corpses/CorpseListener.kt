package de.moritzruth.spigot_ttt.game.corpses

import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.golde.bukkit.corpsereborn.CorpseAPI.events.CorpseClickEvent
import java.time.Instant

object CorpseListener: Listener {
    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) return
        TTTPlayer.of(event.whoClicked as Player) ?: return

        if (CorpseManager.isCorpseInventory(event.inventory)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onCorpseClick(event: CorpseClickEvent) {
        // bug: always ClickType.UNKNOWN
        // if (event.clickType !== ClickType.RIGHT) return

        val tttPlayer = TTTPlayer.of(event.clicker) ?: return
        val tttCorpse = CorpseManager.getTTTCorpse(event.corpse)

        if (tttCorpse !== null) {
            if (Instant.now().toEpochMilli() - tttCorpse.timestamp.toEpochMilli() < 200) return

            if (tttPlayer.alive) tttCorpse.identify(tttPlayer, tttPlayer.role == Role.DETECTIVE)
            event.clicker.openInventory(tttCorpse.inventory)
        }

        event.isCancelled = true
    }
}

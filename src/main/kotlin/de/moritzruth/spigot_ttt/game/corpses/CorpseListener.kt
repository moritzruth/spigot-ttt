package de.moritzruth.spigot_ttt.game.corpses

import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
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
        PlayerManager.getTTTPlayer(event.whoClicked as Player) ?: return

        if (CorpseManager.isCorpseInventory(event.inventory)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onCorpseClick(event: CorpseClickEvent) {
        // bug: always ClickType.UNKNOWN
        // if (event.clickType !== ClickType.RIGHT) return

        val tttPlayer = PlayerManager.getTTTPlayer(event.clicker) ?: return
        val tttCorpse = CorpseManager.getTTTCorpse(event.corpse)

        if (tttCorpse !== null) {
            if(Instant.now().toEpochMilli() - tttCorpse.timestamp.toEpochMilli() < 200) return

            event.clicker.openInventory(tttCorpse.inventory)
            if (tttPlayer.role == Role.DETECTIVE) tttCorpse.inspect(tttPlayer.player)
            else tttCorpse.identify(tttPlayer.player)
        }

        event.isCancelled = true
    }
}

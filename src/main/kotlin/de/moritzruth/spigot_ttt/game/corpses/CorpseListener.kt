package de.moritzruth.spigot_ttt.game.corpses

import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import java.time.Duration
import java.time.Instant

object CorpseListener: Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) return
        TTTPlayer.of(event.whoClicked as Player) ?: return

        if (CorpseManager.isCorpseInventory(event.inventory)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand == EquipmentSlot.OFF_HAND) return
        val tttPlayer = TTTPlayer.of(event.player) ?: return
        val tttCorpse = CorpseManager.getTTTCorpse(event.rightClicked) ?: return

        if (
            Duration.between(tttCorpse.timestamp, Instant.now()).toMillis() < 200 ||
                    event.player.location.distance(tttCorpse.location) > 1.5
        ) return

        if (event.player.inventory.itemInMainHand.type != Material.AIR)
            tttPlayer.ignoreNextInteract = true

        event.isCancelled = true
        plugin.server.pluginManager.callEvent(CorpseClickEvent(tttPlayer, tttCorpse))
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onCorpseClick(event: CorpseClickEvent) {
        if (event.tttPlayer.alive) event.tttCorpse.identify(event.tttPlayer, event.tttPlayer.role == Role.DETECTIVE)
        event.tttPlayer.player.openInventory(event.tttCorpse.inventory)
    }

    @EventHandler
    fun onEntityCombust(event: EntityCombustEvent) {
        if (CorpseManager.getTTTCorpse(event.entity) != null) event.isCancelled = true
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (CorpseManager.getTTTCorpse(event.entity) != null) event.isCancelled = true
    }

    @EventHandler
    fun onEntityTarget(event: EntityTargetEvent) {
        if (event.target != null && CorpseManager.getTTTCorpse(event.entity) != null) event.isCancelled = true
    }
}

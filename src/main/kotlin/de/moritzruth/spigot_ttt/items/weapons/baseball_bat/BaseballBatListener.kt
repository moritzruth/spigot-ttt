package de.moritzruth.spigot_ttt.items.weapons.baseball_bat

import de.moritzruth.spigot_ttt.items.isRelevant
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.Vector

object BaseballBatListener: Listener {
    @EventHandler(ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (!event.isRelevant(BaseballBat.displayName)) return

        val damagerPlayer = event.damager as Player
        val damagedPlayer = event.entity as Player

        val distance = damagerPlayer.location.distance(damagedPlayer.location)

        event.isCancelled = true

        if (distance < 2.5) {
            // Break the item
            val item = damagerPlayer.inventory.itemInMainHand
            val damageableMeta = item.itemMeta!! as Damageable
            damageableMeta.damage = 1000
            item.itemMeta = damageableMeta as ItemMeta

            val direction = damagerPlayer.location.direction

            damagedPlayer.velocity = Vector(direction.x * 3, 8.0, direction.z * 3)
        }
    }
}

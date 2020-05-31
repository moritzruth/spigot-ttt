package de.moritzruth.spigot_ttt.items.weapons.knife

import de.moritzruth.spigot_ttt.items.isRelevant
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

class KnifeListener(val knife: Knife): Listener {
    @EventHandler(ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (!event.isRelevant(Knife.displayName)) return

        val damagerPlayer = event.damager as Player
        val damagedPlayer = event.entity as Player

        val distance = damagerPlayer.location.distance(damagedPlayer.location)

        if (distance > 1.5) event.isCancelled = true else {
            // Break the item
            val item = damagerPlayer.inventory.itemInMainHand
            val damageableMeta = item.itemMeta!! as Damageable
            damageableMeta.damage = 1000
            item.itemMeta = damageableMeta as ItemMeta

            event.damage = 1000.0
        }
    }
}

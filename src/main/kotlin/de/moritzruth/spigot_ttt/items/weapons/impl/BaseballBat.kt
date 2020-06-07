package de.moritzruth.spigot_ttt.items.weapons.impl

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.Selectable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.utils.applyMeta
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.Vector

object BaseballBat: TTTItem, Buyable, Selectable {
    override val type = TTTItem.Type.MELEE
    override val itemStack = ItemStack(CustomItems.baseballBat).applyMeta {
        setDisplayName("${ChatColor.RESET}${ChatColor.BOLD}Baseball-Schläger")
        lore = listOf(
            "",
            "${ChatColor.GOLD}Erhöht die Gechwindigkeit",
            "${ChatColor.GOLD}Schleudert den Gegner weg",
            "",
            "${ChatColor.RED}Nur einmal verwendbar",
            "${ChatColor.RED}Nur aus nächster Nähe"
        )
        addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    }
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL)
    override val price = 1
    override val buyLimit: Int? = null

    override fun onSelect(tttPlayer: TTTPlayer) {
        tttPlayer.player.walkSpeed = 0.3F
    }

    override fun onDeselect(tttPlayer: TTTPlayer) {
        tttPlayer.player.walkSpeed = 0.2F
    }

    override val listener = object : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (!event.isRelevant(BaseballBat)) return

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
}

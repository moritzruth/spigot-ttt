package de.moritzruth.spigot_ttt.items.weapons

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.TRAITOR
import de.moritzruth.spigot_ttt.items.Buyable
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
import java.util.*

object Knife: TTTItem, Buyable {
    override val itemStack = ItemStack(CustomItems.knife).applyMeta {
        setDisplayName("${ChatColor.RED}${ChatColor.BOLD}Knife")
        lore = listOf(
            "",
            "${ChatColor.GRAY}Schaden: ${LoreHelper.damage(null)}",
            "",
            "${ChatColor.RED}Nur einmal verwendbar",
            "${ChatColor.RED}Nur aus nächster Nähe"
        )
        addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    }
    override val buyableBy: EnumSet<TTTPlayer.Role> = EnumSet.of(TRAITOR)
    override val price = 1
    override val type = TTTItem.Type.MELEE

    override val listener = object : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (!event.isRelevant(Knife)) return

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
}



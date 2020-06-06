package de.moritzruth.spigot_ttt.items.impl

import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.*
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.utils.applyMeta
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import java.util.*

object EnderPearl: TTTItem, Buyable {
    override val type = TTTItem.Type.SPECIAL
    override val itemStack = ItemStack(Material.ENDER_PEARL).applyMeta {
        setDisplayName("${ChatColor.DARK_GREEN}Ender Perle")
    }
    override val buyableBy = EnumSet.of(TRAITOR, JACKAL, SIDEKICK, DETECTIVE)
    override val price = 1

    override val listener = object : Listener {
        @EventHandler
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.isRelevant(EnderPearl)) event.isCancelled = true
        }
    }
}

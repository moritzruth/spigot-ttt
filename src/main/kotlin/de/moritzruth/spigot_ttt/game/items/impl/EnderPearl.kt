package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.game.items.Buyable
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.utils.applyMeta
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object EnderPearl: TTTItem, Buyable {
    override val type = TTTItem.Type.SPECIAL
    override val itemStack = ItemStack(Material.ENDER_PEARL).applyMeta {
        setDisplayName("${ChatColor.DARK_GREEN}Enderperle")
    }
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL, Role.DETECTIVE)
    override val price = 1
    override val buyLimit: Int? = null

    override val listener = object : TTTItemListener(this, true) {
        override fun onRightClick(data: ClickEventData) {
            data.event.isCancelled = false
        }
    }
}

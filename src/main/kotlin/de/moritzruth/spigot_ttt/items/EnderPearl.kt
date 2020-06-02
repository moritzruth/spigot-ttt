package de.moritzruth.spigot_ttt.items

import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.*
import de.moritzruth.spigot_ttt.utils.applyMeta
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

object EnderPearl: TTTItem, Buyable {
    override val type = TTTItem.Type.SPECIAL
    override val listener = null
    override val itemStack = ItemStack(Material.ENDER_PEARL).applyMeta {
        setDisplayName("${ChatColor.DARK_GREEN}Ender Perle")
    }
    override val buyableBy = EnumSet.of(TRAITOR, JACKAL, SIDEKICK, DETECTIVE)
    override val price = 1
}

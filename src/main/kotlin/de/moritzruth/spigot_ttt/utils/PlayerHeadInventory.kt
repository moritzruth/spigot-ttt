package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.plugin
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

fun createPlayerHeadInventory(title: String, players: Iterable<Player>) = plugin.server.createInventory(
    null,
    InventoryType.CHEST,
    title
).apply {
    addItem(*players
        .map {
            ItemStack(Material.PLAYER_HEAD).applyMeta {
                setDisplayName("${ChatColor.RESET}${it.displayName}")
                hideInfo()
            }.applyTypedMeta<SkullMeta> {
                owningPlayer = it
            }
        }
        .toTypedArray())
}

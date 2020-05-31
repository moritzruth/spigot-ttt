package de.moritzruth.spigot_ttt.utils

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

fun Inventory.setAllToItem(indexes: Iterable<Int>, itemStack: ItemStack) {
    indexes.forEach { setItem(it, itemStack) }
}

val PlayerInventory.hotbarContents get() = this.contents.slice(0..8) as List<ItemStack?>

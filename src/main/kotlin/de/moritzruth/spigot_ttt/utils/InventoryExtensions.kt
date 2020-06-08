package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

fun Inventory.setAllToItem(indexes: Iterable<Int>, itemStack: ItemStack) {
    indexes.forEach { setItem(it, itemStack) }
}

fun Inventory.removeTTTItem(tttItem: TTTItem) = clear(indexOfFirst { it?.type == tttItem.itemStack.type })
fun Inventory.removeTTTItemNextTick(tttItem: TTTItem) = plugin.server.scheduler.runTask(plugin, fun() {
    removeTTTItem(tttItem)
})

fun PlayerInventory.clearHeldItemSlot() = clear(heldItemSlot)

val PlayerInventory.hotbarContents get() = this.contents.slice(0..8) as List<ItemStack?>

package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.game.items.TTTItem
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

fun Inventory.setAllToItem(indexes: Iterable<Int>, itemStack: ItemStack) {
    indexes.forEach { setItem(it, itemStack) }
}

fun Inventory.removeTTTItem(tttItem: TTTItem) {
    val index = indexOfFirst { it?.type == tttItem.itemStack.type }
    if (index != -1) clear(index)
}
fun Inventory.removeTTTItemNextTick(tttItem: TTTItem) = nextTick { removeTTTItem(tttItem) }

fun PlayerInventory.clearHeldItemSlot() = clear(heldItemSlot)

val PlayerInventory.hotbarContents get() = this.contents.slice(0..8) as List<ItemStack?>

package de.moritzruth.spigot_ttt.shop

import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.BuyableItem
import de.moritzruth.spigot_ttt.items.ItemManager
import org.bukkit.ChatColor

object Shop {
    val SHOP_SLOTS = (9..35).toMutableList().apply {
        // last row; possibly needed in the future
//        remove(17)
//        remove(26)
//        remove(35)
    }.toList()
    private val ITEMS_PER_PAGE = SHOP_SLOTS.count()

    @Suppress("UNCHECKED_CAST")
    fun getBuyableItems(tttPlayer: TTTPlayer): Set<BuyableItem> = ItemManager.items.filter {
        if (it is BuyableItem) it.buyableBy.contains(tttPlayer.role) else false
    }.toSet() as Set<BuyableItem>

    fun show(tttPlayer: TTTPlayer) {
        val itemsIterator = getBuyableItems(tttPlayer).iterator()

        for(index in SHOP_SLOTS) {
            if (!itemsIterator.hasNext()) break

            val tttItem = itemsIterator.next()
            val itemStack = tttItem.itemStack.clone()
            val meta = itemStack.itemMeta!!
            meta.setDisplayName(meta.displayName + "${ChatColor.RESET} - ${ChatColor.BOLD}$${tttItem.price}")
            itemStack.itemMeta = meta

            tttPlayer.player.inventory.setItem(index, itemStack)
        }
    }

    fun hide(tttPlayer: TTTPlayer) {
        val range = 9..19

        range + (1..8)

        for(index in 9..35) tttPlayer.player.inventory.clear(index) // All slots except the hotbar and armor
    }
}

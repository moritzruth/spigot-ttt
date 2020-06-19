package de.moritzruth.spigot_ttt.game.items.shop

import de.moritzruth.spigot_ttt.game.items.ItemManager
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.utils.applyMeta
import org.bukkit.ChatColor

object Shop {
    val SHOP_SLOTS = (9..35).toMutableList().apply {
        // last row; possibly needed in the future
//        remove(17)
//        remove(26)
//        remove(35)
    }.toList()
    private val ITEMS_PER_PAGE = SHOP_SLOTS.count()

    private fun getBuyableItems(tttPlayer: TTTPlayer) = ItemManager.ITEMS
        .filter { it.shopInfo?.run { buyableBy.contains(tttPlayer.role) } ?: false }
        .toSet()

    fun setItems(tttPlayer: TTTPlayer) {
        clear(tttPlayer)
        val itemsIterator = getBuyableItems(tttPlayer).iterator()

        for(index in SHOP_SLOTS) {
            if (!itemsIterator.hasNext()) break

            val tttItem = itemsIterator.next()
            val shopInfo = tttItem.shopInfo!!

            tttPlayer.player.inventory.setItem(index, tttItem.templateItemStack.clone().applyMeta {
                val displayNameSuffix =
                    if (isOutOfStock(tttPlayer, tttItem)) "${ChatColor.RED}Ausverkauft"
                    else "$${shopInfo.price}"

                setDisplayName("$displayName${ChatColor.RESET} - ${ChatColor.BOLD}$displayNameSuffix")
            })
        }
    }

    fun clear(tttPlayer: TTTPlayer) {
        for(index in 9..35) tttPlayer.player.inventory.clear(index) // All slots except the hotbar and armor
    }

    fun isOutOfStock(tttPlayer: TTTPlayer, tttItem: TTTItem<*>): Boolean {
        val shopInfo = tttItem.shopInfo ?: throw Error("Item is not buyable")
        return shopInfo.buyLimit != 0 && tttPlayer.boughtItems.filter { it == tttItem }.count() >= shopInfo.buyLimit
    }
}

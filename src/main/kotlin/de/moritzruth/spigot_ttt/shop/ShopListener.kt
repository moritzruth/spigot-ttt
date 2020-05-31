package de.moritzruth.spigot_ttt.shop

import com.connorlinfoot.actionbarapi.ActionBarAPI
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.BuyableItem
import de.moritzruth.spigot_ttt.items.ItemManager
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

object ShopListener: Listener {
    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) return
        val tttPlayer = PlayerManager.getTTTPlayer(event.whoClicked as Player) ?: return

        if (event.click === ClickType.CREATIVE) return
        event.isCancelled = true

        val itemStack = event.currentItem
        if (
                (event.click === ClickType.RIGHT || event.click === ClickType.LEFT) &&
                itemStack !== null &&
                event.clickedInventory?.holder == tttPlayer.player &&
                Shop.SHOP_SLOTS.contains(event.slot)
        ) {
            val tttItem = ItemManager.getItemByItemStack(itemStack)
            if (tttItem === null || tttItem !is BuyableItem || !tttItem.buyableBy.contains(tttPlayer.role)) return

            if (tttPlayer.credits < tttItem.price) {
                ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}Du hast nicht genug Credits")
            }

            try {
                tttPlayer.addItem(tttItem)
                tttPlayer.credits -= tttItem.price
            } catch (e: TTTPlayer.AlreadyHasItemException) {
                ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}Du hast dieses Item bereits")
            } catch (e: TTTPlayer.TooManyItemsOfTypeException) {
                ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}Du hast keinen Platz dafür")
            }
        }
    }
}

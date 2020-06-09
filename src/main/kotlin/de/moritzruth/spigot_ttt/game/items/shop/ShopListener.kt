package de.moritzruth.spigot_ttt.game.items.shop

import com.connorlinfoot.actionbarapi.ActionBarAPI
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayerDeathEvent
import de.moritzruth.spigot_ttt.game.items.Buyable
import de.moritzruth.spigot_ttt.game.items.ItemManager
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

        if (event.click === ClickType.CREATIVE || event.clickedInventory?.holder != event.whoClicked) return
        event.isCancelled = true

        val itemStack = event.currentItem
        if (
                (event.click === ClickType.RIGHT || event.click === ClickType.LEFT) &&
                itemStack !== null &&
                event.clickedInventory?.holder == tttPlayer.player &&
                Shop.SHOP_SLOTS.contains(event.slot)
        ) {
            val tttItem = ItemManager.getItemByItemStack(itemStack)
            if (tttItem === null || tttItem !is Buyable || !tttItem.buyableBy.contains(tttPlayer.role)) return

            when {
                Shop.isOutOfStock(tttPlayer, tttItem) ->
                    ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}Dieses Item ist ausverkauft")

                tttPlayer.credits < tttItem.price ->
                    ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}Du hast nicht genug Credits")

                else -> try {
                    tttPlayer.addItem(tttItem)
                    tttPlayer.boughtItems.add(tttItem)
                    tttPlayer.credits -= tttItem.price

                    tttItem.onBuy(tttPlayer)

                    Shop.setItems(tttPlayer)
                } catch (e: TTTPlayer.AlreadyHasItemException) {
                    ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}Du hast dieses Item bereits")
                } catch (e: TTTPlayer.TooManyItemsOfTypeException) {
                    ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}Du hast keinen Platz dafür")
                }
            }
        }
    }

    @EventHandler
    fun onTTTPlayerDeath(event: TTTPlayerDeathEvent) {
        val killer = event.killer ?: return

        if (event.tttPlayer.role.group != killer.role.group) {
            PlayerManager.tttPlayers
                .filter { it.role.canOwnCredits && it.role.group == killer.role.group }
                .forEach {
                    it.credits += 1
                    ActionBarAPI.sendActionBar(it.player, "${ChatColor.GREEN}Du hast einen Credit erhalten")
                }
        }
    }
}

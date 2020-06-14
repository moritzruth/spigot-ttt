package de.moritzruth.spigot_ttt.game.items.shop

import de.moritzruth.spigot_ttt.Settings
import de.moritzruth.spigot_ttt.game.items.Buyable
import de.moritzruth.spigot_ttt.game.items.ItemManager
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayerTrueDeathEvent
import de.moritzruth.spigot_ttt.utils.sendActionBarMessage
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

object ShopListener: Listener {
    @EventHandler(ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) return
        val tttPlayer = TTTPlayer.of(event.whoClicked as Player) ?: return

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
                    tttPlayer.player.sendActionBarMessage("${ChatColor.RED}Dieses Item ist ausverkauft")

                tttPlayer.credits < tttItem.price ->
                    tttPlayer.player.sendActionBarMessage("${ChatColor.RED}Du hast nicht genug Credits")

                else -> try {
                    tttPlayer.addItem(tttItem)
                    tttPlayer.boughtItems.add(tttItem)
                    tttPlayer.credits -= tttItem.price

                    Shop.setItems(tttPlayer)
                } catch (e: TTTPlayer.AlreadyHasItemException) {
                    tttPlayer.player.sendActionBarMessage("${ChatColor.RED}Du hast dieses Item bereits")
                } catch (e: TTTPlayer.TooManyItemsOfTypeException) {
                    tttPlayer.player.sendActionBarMessage("${ChatColor.RED}Du hast keinen Platz dafür")
                }
            }
        }
    }

    @EventHandler
    fun onTTTPlayerDeath(event: TTTPlayerTrueDeathEvent) {
        val killer = event.killer ?: return

        if (event.tttPlayer.role.group != killer.role.group) {
            PlayerManager.tttPlayers
                .filter { it.role.canOwnCredits && it.role.group == killer.role.group }
                .forEach {
                    it.credits += Settings.creditsPerKill
                    it.player.sendActionBarMessage("${ChatColor.GREEN}Du hast ${Settings.creditsPerKill} Credit(s) erhalten")
                }
        }
    }
}

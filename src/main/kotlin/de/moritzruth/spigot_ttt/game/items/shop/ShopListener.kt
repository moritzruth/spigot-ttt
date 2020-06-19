package de.moritzruth.spigot_ttt.game.items.shop

import de.moritzruth.spigot_ttt.Settings
import de.moritzruth.spigot_ttt.game.GameListener
import de.moritzruth.spigot_ttt.game.items.ItemManager
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayerTrueDeathEvent
import de.moritzruth.spigot_ttt.utils.sendActionBarMessage
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

object ShopListener: GameListener() {
    @EventHandler(ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) = handle(event) { tttPlayer ->
        if (event.click === ClickType.CREATIVE || event.clickedInventory?.holder != event.whoClicked) return@handle
        event.isCancelled = true

        val itemStack = event.currentItem
        if (
                (event.click === ClickType.RIGHT || event.click === ClickType.LEFT) &&
                itemStack !== null &&
                event.clickedInventory?.holder == tttPlayer.player &&
                Shop.SHOP_SLOTS.contains(event.slot)
        ) {
            val tttItem = ItemManager.getTTTItemByItemStack(itemStack) ?: return@handle
            val shopMeta = tttItem.shopInfo
            if (shopMeta == null || !shopMeta.buyableBy.contains(tttPlayer.role)) return@handle

            when {
                Shop.isOutOfStock(tttPlayer, tttItem) ->
                    tttPlayer.player.sendActionBarMessage("${ChatColor.RED}Dieses Item ist ausverkauft")

                tttPlayer.credits < tttItem.shopInfo.price ->
                    tttPlayer.player.sendActionBarMessage("${ChatColor.RED}Du hast nicht genug Credits")

                else -> try {
                    tttPlayer.addItem(tttItem)
                    tttPlayer.boughtItems.add(tttItem)
                    tttPlayer.credits -= shopMeta.price
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
                    val creditsPerKill = Settings.creditsPerKill
                    it.credits += creditsPerKill
                    it.player.sendActionBarMessage("${ChatColor.GREEN}Du hast $creditsPerKill Credit(s) erhalten")
                }
        }
    }
}

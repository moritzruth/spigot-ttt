package de.moritzruth.spigot_ttt.shop

import com.connorlinfoot.actionbarapi.ActionBarAPI
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.ItemManager
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitTask

object Shop {
    val SHOP_SLOTS = (9..35).toMutableList().apply {
        // last row; possibly needed in the future
//        remove(17)
//        remove(26)
//        remove(35)
    }.toList()
    private val ITEMS_PER_PAGE = SHOP_SLOTS.count()

    private var creditsTimer: BukkitTask? = null

    fun getBuyableItems(tttPlayer: TTTPlayer) = ItemManager.items.filter { it is Buyable && it.buyableBy.contains(tttPlayer.role) }.toSet()

    fun show(tttPlayer: TTTPlayer) {
        val itemsIterator = getBuyableItems(tttPlayer).iterator()

        for(index in SHOP_SLOTS) {
            if (!itemsIterator.hasNext()) break

            val tttItem = itemsIterator.next()
            val itemStack = tttItem.itemStack.clone()
            val meta = itemStack.itemMeta!!
            meta.setDisplayName(meta.displayName + "${ChatColor.RESET} - ${ChatColor.BOLD}$${(tttItem as Buyable).price}")
            itemStack.itemMeta = meta

            tttPlayer.player.inventory.setItem(index, itemStack)
        }
    }

    fun hide(tttPlayer: TTTPlayer) {
        val range = 9..19

        range + (1..8)

        for(index in 9..35) tttPlayer.player.inventory.clear(index) // All slots except the hotbar and armor
    }

    fun startCreditsTimer() {
        val firstCreditsAfter = plugin.config.getInt("first-credits-after")
        val interval = plugin.config.getInt("credits-interval")

        creditsTimer = plugin.server.scheduler.runTaskTimer(plugin, fun() {
            PlayerManager.tttPlayers.filter { it.alive && it.role.canOwnCredits }.forEach {
                it.credits += 1
                ActionBarAPI.sendActionBar(it.player, "${ChatColor.GREEN}Du hast einen Credit erhalten")
            }
        }, secondsToTicks(firstCreditsAfter).toLong(), secondsToTicks(interval).toLong())
    }

    fun stopCreditsTimer() {
        creditsTimer?.cancel()
    }
}

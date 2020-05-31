package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.TTTPlugin
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.corpses.CorpseManager
import de.moritzruth.spigot_ttt.items.ItemManager
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.golde.bukkit.corpsereborn.CorpseAPI.events.CorpseClickEvent
import java.time.Instant

object GeneralGameEventsListener: Listener {
    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage = "${TTTPlugin.prefix}${event.player.displayName} ${ChatColor.GOLD}hat das Spiel betreten."
        PlayerManager.onPlayerJoin(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage = "${TTTPlugin.prefix}${event.player.displayName} ${ChatColor.GOLD}hat das Spiel verlassen."
        PlayerManager.onPlayerQuit(event.player)
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        event.foodLevel = 20
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onEntityDamageLow(event: EntityDamageEvent) {
        if (GameManager.phase !== GamePhase.COMBAT) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDamageHighest(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        val tttPlayer = PlayerManager.getTTTPlayer(event.entity as Player) ?: return

        if (tttPlayer.player.health - event.finalDamage <= 0) {
            tttPlayer.kill()

//                gameManager.playerManager.tttPlayers.forEach {
//                    it.player.playSound(tttPlayer.player.location, Sound.ENTITY_PLAYER_DEATH, SoundCategory.PLAYERS, 2f, 1f)
//                }

            event.damage = 0.0
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.deathMessage = null
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.player.gameMode !== GameMode.CREATIVE) event.isCancelled = true
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.player.gameMode !== GameMode.CREATIVE) event.isCancelled = true
    }

    @EventHandler
    fun onCorpseClick(event: CorpseClickEvent) {
        // bug: always ClickType.UNKNOWN
        // if (event.clickType !== ClickType.RIGHT) return

        val tttCorpse = CorpseManager.getTTTCorpse(event.corpse)

        if (tttCorpse !== null) {
            if(Instant.now().toEpochMilli() - tttCorpse.timestamp.toEpochMilli() < 200) return

            event.clicker.openInventory(tttCorpse.inventory)
            tttCorpse.identify(event.clicker)
        }

        event.isCancelled = true
    }

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        val senderTTTPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

        if (!senderTTTPlayer.alive) {
            PlayerManager.tttPlayers.filter { !it.alive }.forEach {
                it.player.sendMessage("${ChatColor.GRAY}[${ChatColor.RED}TOT${ChatColor.GRAY}] <${event.player.displayName}> ${event.message}")
            }

            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

        val itemStack = event.player.inventory.getItem(event.newSlot)

        tttPlayer.itemInHand =
                if (itemStack == null || itemStack.type === Material.AIR) null
                else ItemManager.getItemByItemStack(itemStack)
    }

    @EventHandler
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        if (event.entity !is Player) {
            return
        }

        val player = event.entity as Player
        val tttPlayer = PlayerManager.getTTTPlayer(player) ?: return

        val tttItem = ItemManager.getItemByItemStack(event.item.itemStack)

        if (tttItem != null) {
            if (kotlin.runCatching { tttPlayer.checkAddItemPreconditions(tttItem) }.isSuccess) {
                plugin.server.scheduler.runTask(plugin, fun() {
                    tttPlayer.updateItemInHand()
                })

                return
            }
        }

        event.isCancelled = true
    }
}

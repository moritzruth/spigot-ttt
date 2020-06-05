package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.TTTPlugin
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.PlayerManager
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
import org.bukkit.event.entity.*
import org.bukkit.event.player.*

object GeneralGameEventsListener: Listener {
    val BLOCKED_COMMANDS = setOf("me", "tell")

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

    @EventHandler
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        if(event.message.startsWith("/rl") && GameManager.phase != null) { // /reload is not blocked
            event.player.sendMessage(TTTPlugin.prefix + "${ChatColor.RED}The server may not be reloaded while the game is running")
            event.player.sendMessage(TTTPlugin.prefix + "${ChatColor.RED}You can force reload by using ${ChatColor.WHITE}/reload")
            event.isCancelled = true
            return
        }

        if (BLOCKED_COMMANDS.find { event.message.startsWith("/$it") } != null) {
            if (GameManager.phase != null) {
                event.player.sendMessage(TTTPlugin.prefix + "${ChatColor.RED}Dieser Befehl ist blockiert.")
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val player = event.damager
        if (player is Player) {
            if (player.inventory.itemInMainHand.type == Material.AIR) {
                event.damage = 0.2
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onEntityDamageLow(event: EntityDamageEvent) {
        if (GameManager.phase !== GamePhase.COMBAT) {
            event.isCancelled = true
        }

        val player = event.entity
        if (player is Player) {
            plugin.server.scheduler.runTask(plugin, fun() {
                player.noDamageTicks = 0
            })
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDamageHighest(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        val tttPlayer = PlayerManager.getTTTPlayer(event.entity as Player) ?: return

        if (tttPlayer.player.health - event.finalDamage <= 0) {
            val damageInfo = tttPlayer.damageInfo

            val reason = if (damageInfo != null) {
                tttPlayer.damageInfo = null
                damageInfo.damager.credits += 1

                damageInfo.deathReason
            } else when(event.cause) {
                EntityDamageEvent.DamageCause.FALL -> DeathReason.FALL
                EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION -> DeathReason.EXPLOSION
                EntityDamageEvent.DamageCause.DROWNING -> DeathReason.DROWNED
                EntityDamageEvent.DamageCause.FIRE,
                EntityDamageEvent.DamageCause.FIRE_TICK,
                EntityDamageEvent.DamageCause.LAVA,
                EntityDamageEvent.DamageCause.HOT_FLOOR -> DeathReason.FIRE
                EntityDamageEvent.DamageCause.POISON, EntityDamageEvent.DamageCause.WITHER -> DeathReason.POISON
                else -> DeathReason.SUICIDE
            }

            tttPlayer.kill(reason)

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

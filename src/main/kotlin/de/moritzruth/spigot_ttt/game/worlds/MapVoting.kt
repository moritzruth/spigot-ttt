package de.moritzruth.spigot_ttt.game.worlds

import de.moritzruth.spigot_ttt.Settings
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.*
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.math.max

class MapVoting private constructor() {
    private var secondsRemaining = Settings.mapVotingDuration
    private val maps = WorldManager.tttWorlds.toList()
    private var timerTask: BukkitTask? = null

    private val inventory = plugin.server.createInventory(
        null,
        InventoryType.CHEST,
        "${ChatColor.BLUE}${ChatColor.BOLD}Map-Voting"
    )

    private val votes = mutableMapOf<UUID, TTTWorld>()

    private fun createMapItemStack(map: TTTWorld): ItemStack {
        val config = map.config
        val iconMaterialString = config.getString("icon") ?: "GRASS_BLOCK"
        val votesForThisMap = votes.values.count { it === map }

        return ItemStack(Material.valueOf(iconMaterialString), max(1, votesForThisMap)).applyMeta {
            setDisplayName("${config.getString("title")}${ChatColor.RESET} ${ChatColor.GRAY}($votesForThisMap)")
            lore = listOf("").plus(config.getStringList("description").map { "${ChatColor.RESET}$it" })
            hideInfo()
        }
    }

    init {
        when (maps.count()) {
            0 -> throw Error("There are no worlds available")
            1 -> finish(maps[0])
            else -> {
                current = this
                maps.forEachIndexed { index, map -> inventory.setItem(index, createMapItemStack(map)) }

                timerTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
                    if (secondsRemaining == 0) {
                        finish()
                    } else {
                        inventory.setItem(26, ItemStack(Material.CLOCK, secondsRemaining).applyMeta {
                            setDisplayName("${ChatColor.GREEN}Verbleibende Zeit: ${ChatColor.WHITE}${secondsRemaining}s")
                        })
                        secondsRemaining -= 1
                    }
                }, 0, secondsToTicks(1).toLong())

                PlayerManager.getAvailablePlayers().forEach(::giveVoteItem)
                plugin.broadcast("${ChatColor.GREEN}Das Map-Voting wurde gestartet.")
            }
        }
    }

    fun vote(player: Player, map: TTTWorld) {
        votes[player.uniqueId] = map
        inventory.setItem(maps.indexOf(map), createMapItemStack(map))

        if (votes.keys.containsAll(PlayerManager.getAvailablePlayers().map { it.uniqueId })) finish()
    }

    fun cancel() {
        plugin.broadcast("${ChatColor.RED}Das Map-Voting wurde abgebrochen.")
        stop()
    }

    private fun stop() {
        timerTask?.cancel()
        current = null
        PlayerManager.getAvailablePlayers().forEach { removeVoteItem(it) }
        plugin.server.onlinePlayers.forEach { if (it.openInventory.topInventory === inventory) it.closeInventory() }
    }

    private fun finish(force: TTTWorld? = null) {
        stop()
        val winnerMap = force ?: votes.values.let { votedMaps ->
            if (votedMaps.count() == 0) maps.random()
            else votedMaps.sortedBy { votedMap -> votedMaps.count { it === votedMap } }[0]
        }

        if (runCatching { PlayerManager.checkEnoughPlayers() }.isFailure) {
            plugin.broadcast(
                "${ChatColor.RED}Das Spiel konnte nicht gestartet werden, da nicht genügend " +
                        "Spieler online sind."
            )
        } else {
            plugin.broadcast("${ChatColor.GREEN}Ausgewählte Map: " +
                    winnerMap.config.getString("title"))

            if (winnerMap.world != null) winnerMap.unload()
            winnerMap.load()

            GameManager.tttWorld = winnerMap
            plugin.server.onlinePlayers.forEach { it.teleport(winnerMap.world!!.spawnLocation) }
            GameManager.startPreparingPhase()
        }
    }

    companion object {
        var current: MapVoting? = null; private set

        private val voteItem = ItemStack(Material.PAPER).applyMeta {
            setDisplayName("${ChatColor.RESET}${ChatColor.BOLD}Map-Voting")
            hideInfo()
        }

        private fun giveVoteItem(player: Player) {
            player.inventory.setItem(8, voteItem)
        }

        private fun removeVoteItem(player: Player) {
            player.inventory.clear(8)
        }

        fun start(): MapVoting? {
            if (current != null) throw IllegalStateException("There is already a map voting in progress")
            return MapVoting()
        }

        private val listener = object : Listener {
            @EventHandler
            fun onPlayerInteract(event: PlayerInteractEvent) {
                if (event.item?.type == Material.PAPER && event.action.isRightClick) {
                    val voting = current ?: return
                    event.player.openInventory(voting.inventory)
                }
            }

            @EventHandler
            fun onInventoryClick(event: InventoryClickEvent) {
                val whoClicked = event.whoClicked
                if (whoClicked !is Player) return

                val voting = current ?: return
                if (event.clickedInventory != voting.inventory) return
                event.isCancelled = true

                val map = voting.maps.getOrNull(event.slot) ?: return
                if (event.isShiftClick && whoClicked.hasPermission("ttt.force-map")) {
                    voting.finish(force = map)
                } else if (event.click.isLeftClick) {
                    voting.vote(whoClicked, map)
                }
            }

            @EventHandler
            fun onPlayerDropItem(event: PlayerDropItemEvent) {
                if (current != null && event.itemDrop.itemStack.type == Material.PAPER) {
                    event.isCancelled = true
                }
            }

            @EventHandler
            fun onPlayerGameModeChange(event: PlayerGameModeChangeEvent) {
                nextTick {
                    if (current != null) {
                        if (PlayerManager.isAvailable(event.player)) giveVoteItem(event.player)
                        else removeVoteItem(event.player)
                    }
                }
            }

            @EventHandler
            fun onPlayerJoinEvent(event: PlayerJoinEvent) {
                if (PlayerManager.isAvailable(event.player)) {
                    if (current == null) removeVoteItem(event.player)
                    else giveVoteItem(event.player)
                }
            }
        }

        fun registerListener() {
            plugin.server.pluginManager.registerEvents(listener, plugin)
        }
    }
}

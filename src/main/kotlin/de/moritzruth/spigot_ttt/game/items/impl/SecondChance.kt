package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.GameEndEvent
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.items.Buyable
import de.moritzruth.spigot_ttt.game.items.PASSIVE
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.nextTick
import de.moritzruth.spigot_ttt.utils.setAllToItem
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

object SecondChance: TTTItem, Buyable {
    private val DISPLAY_NAME = "${ChatColor.GREEN}${ChatColor.BOLD}Second Chance"
    val ON_CORPSE = Resourcepack.Items.arrowDown
    val ON_SPAWN = Resourcepack.Items.dot
    private const val TIMEOUT = 10.0

    override val type = TTTItem.Type.SPECIAL
    override val itemStack = ItemStack(Resourcepack.Items.secondChance).applyMeta {
        setDisplayName("$DISPLAY_NAME $PASSIVE")
        hideInfo()
        lore = listOf(
            "",
            "${ChatColor.GOLD}Du wirst mit einer 50%-Chance",
            "${ChatColor.GOLD}wiederbelebt, wenn du stirbst"
        )
    }
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL)
    override val buyLimit = 1
    override val price = 2
    val isc = InversedStateContainer(State::class)

    private val chooseSpawnInventory = plugin.server.createInventory(
        null,
        InventoryType.CHEST,
        "${ChatColor.DARK_RED}${ChatColor.BOLD}Second Chance"
    ).apply {
        setAllToItem(setOf(0, 1, 2, 9, 10, 11, 18, 19, 20), ItemStack(ON_CORPSE).applyMeta {
            hideInfo()
            setDisplayName("${ChatColor.GREEN}${ChatColor.BOLD}Bei der Leiche")
        })

        setAllToItem(setOf(3, 4, 5, 12, 13, 14, 21, 22, 23), ItemStack(Resourcepack.Items.textureless).applyMeta {
            hideInfo()
            setDisplayName("${ChatColor.RESET}${ChatColor.BOLD}Wo möchtest du spawnen?")
        })

        setAllToItem(setOf(6, 7, 8, 15, 16, 17, 24, 25, 26), ItemStack(ON_SPAWN).applyMeta {
            hideInfo()
            setDisplayName("${ChatColor.AQUA}${ChatColor.BOLD}Am Spawn")
        })
    }

    override fun onBuy(tttPlayer: TTTPlayer) {
        isc.getOrCreate(tttPlayer)
    }

    override val listener = object : TTTItemListener(this, true) {
        @EventHandler
        fun onTTTPlayerDeath(event: TTTPlayerDeathEvent) {
            val state = isc.get(event.tttPlayer)
            if (state != null) {
                if (Random.nextBoolean()) {
                    event.winnerRoleGroup = null
                    event.tttPlayer.player.openInventory(chooseSpawnInventory)
                    state.timeoutAction = TimeoutAction(event.tttPlayer, event.tttCorpse.corpse.trueLocation)
                }
            }
        }

        @EventHandler
        fun onInventoryClose(event: InventoryCloseEvent) = handle(event) { tttPlayer ->
            if (event.inventory == chooseSpawnInventory) {
                if (isc.get(tttPlayer)?.timeoutAction != null) {
                    nextTick { if (isc.get(tttPlayer) != null) tttPlayer.player.openInventory(chooseSpawnInventory) }
                }
            }
        }

        @EventHandler
        fun onInventoryClick(event: InventoryClickEvent) = handle(event) { tttPlayer ->
            if (event.clickedInventory != chooseSpawnInventory) return@handle
            val state = isc.get(tttPlayer) ?: return@handle
            val timeoutAction = state.timeoutAction ?: return@handle

            val location = when (event.currentItem?.type) {
                ON_SPAWN -> GameManager.world.spawnLocation
                ON_CORPSE -> timeoutAction.deathLocation
                else -> return@handle
            }

            timeoutAction.stop()
            tttPlayer.revive(location)
        }

        @EventHandler
        fun onTTTPlayerRevive(event: TTTPlayerReviveEvent) {
            isc.get(event.tttPlayer)?.timeoutAction?.stop()
        }

        @EventHandler
        fun onGameEnd(event: GameEndEvent) {
            isc.forEveryState { state, _ -> state.timeoutAction?.stop() }
        }
    }

    class TimeoutAction(
        private val tttPlayer: TTTPlayer,
        val deathLocation: Location
    ) {
        private val startedAt = Instant.now()!!
        private var bossBar = plugin.server.createBossBar(
            "${ChatColor.GREEN}${ChatColor.BOLD}Second Chance",
            BarColor.GREEN,
            BarStyle.SOLID
        ).also { it.addPlayer(tttPlayer.player) }

        private var task: BukkitTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
            val duration = Duration.between(startedAt, Instant.now()).toMillis().toDouble() / 1000
            val progress = duration / TIMEOUT

            if (progress > 1) stop() else bossBar.progress = 1.0 - progress
        }, 0, 1)

        fun stop() {
            isc.remove(tttPlayer)
            task.cancel()
            tttPlayer.player.closeInventory()
            bossBar.removePlayer(tttPlayer.player)

            try {
                PlayerManager.letRemainingRoleGroupWin()
            } catch (e: IllegalStateException) {}
        }
    }

    class State: IState {
        var timeoutAction: TimeoutAction? = null
    }
}

package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
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

object SecondChance: TTTItem<SecondChance.Instance>(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    templateItemStack = ItemStack(Resourcepack.Items.secondChance).applyMeta {
        setDisplayName("${ChatColor.GREEN}${ChatColor.BOLD}Second Chance$PASSIVE_SUFFIX")
        hideInfo()
        lore = listOf(
            "",
            "${ChatColor.GOLD}Du wirst mit einer 50%-Chance",
            "${ChatColor.GOLD}wiederbelebt, wenn du stirbst"
        )
    },
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.JACKAL),
        buyLimit = 1,
        price = 2
    ),
    removeInstanceOnDeath = false
) {
    val ON_CORPSE = Resourcepack.Items.arrowDown
    val ON_SPAWN = Resourcepack.Items.dot
    private const val TIMEOUT = 10.0

    override fun getInstance(tttPlayer: TTTPlayer) =
        instancesByUUID.values.find { it.tttPlayer === tttPlayer }

    class Instance: TTTItem.Instance(SecondChance, false) {
        var preventRoundEnd = false; private set
        var timeoutAction: TimeoutAction? = null
        lateinit var tttPlayer: TTTPlayer

        fun possiblyTrigger() {
            if (Random.nextBoolean()) trigger()
        }

        private fun trigger() {
            preventRoundEnd = true
            timeoutAction = TimeoutAction(this)
        }

        override fun reset() {
            timeoutAction?.stop()
        }

        override fun onCarrierSet(carrier: TTTPlayer, isFirst: Boolean) {
            tttPlayer = carrier
        }

        class TimeoutAction(private val instance: Instance) {
            val deathLocation: Location = instance.requireCarrier().player.location
            private val startedAt = Instant.now()!!
            private var bossBar = plugin.server.createBossBar(
                "${ChatColor.GREEN}${ChatColor.BOLD}Second Chance",
                BarColor.GREEN,
                BarStyle.SOLID
            ).also { it.addPlayer(instance.tttPlayer.player) }

            init {
                instance.tttPlayer.player.openInventory(chooseSpawnInventory)
            }

            private var task: BukkitTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
                val duration = Duration.between(startedAt, Instant.now()).toMillis().toDouble() / 1000
                val progress = duration / TIMEOUT

                if (progress > 1) onTimeout() else bossBar.progress = 1.0 - progress
            }, 0, 1)

            private fun onTimeout() {
                try {
                    PlayerManager.letRemainingRoleGroupWin()
                } catch (e: IllegalStateException) {}
                stop()
            }

            fun stop() {
                task.cancel()
                instance.tttPlayer.player.apply {
                    closeInventory()
                    bossBar.removePlayer(this)
                }
                instancesByUUID.remove(instance.uuid)
            }
        }
    }

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

    override val listener = object : TTTItemListener<Instance>(this) {
        @EventHandler
        fun onTTTPlayerTrueDeath(event: TTTPlayerTrueDeathEvent) {
            val instance = getInstance(event.tttPlayer) ?: return
            instance.possiblyTrigger()
            if (instancesByUUID.values.find { it.preventRoundEnd } != null) event.winnerRoleGroup = null
        }

        @EventHandler
        fun onInventoryClose(event: InventoryCloseEvent) {
            if (event.inventory == chooseSpawnInventory) {
                nextTick {
                    handleWithInstance(event) { instance ->
                        instance.tttPlayer.player.openInventory(chooseSpawnInventory)
                    }
                }
            }
        }

        @EventHandler
        fun onInventoryClick(event: InventoryClickEvent) {
            if (event.clickedInventory != chooseSpawnInventory) return

            handleWithInstance(event) { instance ->
                val timeoutAction = instance.timeoutAction!!

                val location = when (event.currentItem?.type) {
                    ON_SPAWN -> GameManager.world.spawnLocation
                    ON_CORPSE -> timeoutAction.deathLocation
                    else -> return@handleWithInstance
                }

                timeoutAction.stop()
                instance.tttPlayer.revive(location)
            }
        }

        @EventHandler
        fun onTTTPlayerRevive(event: TTTPlayerReviveEvent) = handle(event) { it.timeoutAction?.stop() }
    }
}

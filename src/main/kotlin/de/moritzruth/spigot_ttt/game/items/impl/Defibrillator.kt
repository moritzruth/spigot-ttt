package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.corpses.CorpseClickEvent
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.*
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.event.EventHandler
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import java.time.Instant

object Defibrillator: TTTItem<Defibrillator.Instance>(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    templateItemStack = ItemStack(Resourcepack.Items.defibrillator).applyMeta {
        setDisplayName("${ChatColor.RESET}${ChatColor.BOLD}Defibrillator")
        hideInfo()
        lore = listOf(
            "",
            "${ChatColor.GOLD}Belebe einen Spieler wieder"
        )
    },
    shopInfo = ShopInfo(
        buyLimit = 1,
        price = 2,
        buyableBy = roles(Role.TRAITOR, Role.DETECTIVE, Role.JACKAL)
    )
) {
    private const val REVIVE_DURATION = 10.0

    class Instance: TTTItem.Instance(Defibrillator) {
        var action: Action? = null
        var bossBar = plugin.server.createBossBar(
            "${ChatColor.BOLD}Defibrillator",
            BarColor.GREEN,
            BarStyle.SOLID
        ).also { it.isVisible = false }

        override fun onCarrierSet(carrier: TTTPlayer, isFirst: Boolean) {
            bossBar.addPlayer(carrier.player)
        }

        override fun onCarrierRemoved(oldCarrier: TTTPlayer) {
            bossBar.removePlayer(oldCarrier.player)
        }

        override fun reset() {
            action?.reset()
            stopSound()
        }
    }

    fun stopSound() = plugin.server.onlinePlayers.forEach {
        it.stopSound(Resourcepack.Sounds.Item.Defibrillator.use, SoundCategory.PLAYERS)
    }

    override val listener = object : TTTItemListener<Instance>(this) {
        @EventHandler(ignoreCancelled = true)
        fun onCorpseClick(event: CorpseClickEvent) {
            val instance = getInstance(event.tttPlayer.player.inventory.itemInMainHand) ?: return
            event.isCancelled = true

            when(val action = instance.action) {
                null -> instance.action = Action.Reviving(event.tttPlayer, instance)
                is Action.Reviving -> {
                    action.cancelTask.cancel()
                    action.cancelTask = action.createCancelTask()

                    val progress = action.duration / REVIVE_DURATION
                    if (progress >= 1) {
                        try {
                            event.tttCorpse.revive()
                            event.tttPlayer.player.sendActionBarMessage(
                                "${ChatColor.BOLD}${event.tttCorpse.tttPlayer.player.displayName} " +
                                        "${ChatColor.GREEN}wurde wiederbelebt"
                            )

                            action.cancelTask.cancel()
                            event.tttPlayer.removeItem(Defibrillator)
                        } catch(e: TTTPlayer.AlreadyLivingException) {
                            action.cancel()
                        }
                    } else instance.bossBar.progress = progress
                }
                is Action.Canceled -> noop()
            }
        }
    }

    sealed class Action(val tttPlayer: TTTPlayer) {
        open fun reset() {}

        class Reviving(tttPlayer: TTTPlayer, val instance: Instance): Action(tttPlayer) {
            var cancelTask = createCancelTask()
            private val startedAt: Instant = Instant.now()
            val duration get() = Duration.between(startedAt, Instant.now()).toMillis().toDouble() / 1000

            fun createCancelTask() = plugin.server.scheduler.runTaskLater(plugin, fun() {
                cancel()
            }, 10)

            fun cancel() {
                cancelTask.cancel()
                instance.action = Canceled(tttPlayer, instance)
            }

            init {
                GameManager.world.playSound(
                    tttPlayer.player.location,
                    Resourcepack.Sounds.Item.Defibrillator.use,
                    SoundCategory.PLAYERS,
                    0.8F,
                    1F
                )

                instance.bossBar.color = BarColor.GREEN
                instance.bossBar.isVisible = true
            }

            override fun reset() = cancelTask.cancel()
        }

        class Canceled(tttPlayer: TTTPlayer, val instance: Instance): Action(tttPlayer) {
            private var switches: Int = 0
            private lateinit var task: BukkitTask

            init {
                stopSound()
                GameManager.world.playSound(
                    tttPlayer.player.location,
                    Resourcepack.Sounds.Item.Defibrillator.failed,
                    SoundCategory.PLAYERS,
                    1F,
                    1F
                )

                plugin.server.scheduler.runTaskTimer(plugin, { task ->
                    if (switches == SWITCHES_COUNT) {
                        this@Canceled.task = task
                        task.cancel()
                        instance.action = null
                        instance.bossBar.isVisible = false
                    } else {
                        instance.bossBar.progress = 1.0
                        instance.bossBar.color = if (switches % 2 == 0) BarColor.RED else BarColor.WHITE
                        switches += 1
                    }
                }, 0, secondsToTicks(0.2).toLong())
            }

            override fun reset() = task.cancel()

            companion object {
                const val SWITCHES_COUNT = 6
            }
        }
    }
}

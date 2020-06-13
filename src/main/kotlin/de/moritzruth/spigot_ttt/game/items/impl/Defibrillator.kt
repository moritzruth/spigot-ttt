package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameEndEvent
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.corpses.CorpseClickEvent
import de.moritzruth.spigot_ttt.game.items.Buyable
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.*
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

object Defibrillator: TTTItem, Buyable {
    private const val REVIVE_DURATION = 10.0

    override val type = TTTItem.Type.SPECIAL
    override val itemStack = ItemStack(Resourcepack.Items.defibrillator).applyMeta {
        setDisplayName("${ChatColor.RESET}${ChatColor.BOLD}Defibrillator")
        hideInfo()
        lore = listOf(
            "",
            "${ChatColor.GOLD}Belebe einen Spieler wieder"
        )
    }
    override val buyableBy = roles(Role.TRAITOR, Role.DETECTIVE, Role.JACKAL)
    override val price = 2
    override val buyLimit = 1

    private val isc = InversedStateContainer(State::class)

    fun stopSound() = plugin.server.onlinePlayers.forEach {
        it.stopSound(Resourcepack.Sounds.Item.Defibrillator.use, SoundCategory.PLAYERS)
    }

    override val listener = object : TTTItemListener(this, true) {
        @EventHandler(ignoreCancelled = true)
        fun onCorpseClick(event: CorpseClickEvent) {
            if (event.tttPlayer.player.inventory.itemInMainHand.type != itemStack.type) return
            event.isCancelled = true

            val state = isc.getOrCreate(event.tttPlayer)
            state.bossBar.addPlayer(event.tttPlayer.player)

            when(val action = state.action) {
                null -> state.action = Action.Reviving(event.tttPlayer, state)
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
                            event.tttPlayer.player.inventory.removeTTTItemNextTick(Defibrillator)
                            state.reset(event.tttPlayer)
                            isc.remove(event.tttPlayer)
                        } catch(e: TTTPlayer.AlreadyLivingException) {
                            action.cancel()
                        }
                    } else state.bossBar.progress = progress
                }
                is Action.Canceled -> noop()
            }
        }

        @EventHandler
        fun onGameEnd(event: GameEndEvent) = isc.forEveryState { state, tttPlayer -> state.reset(tttPlayer) }
    }

    sealed class Action(val tttPlayer: TTTPlayer) {
        open fun reset() {}

        class Reviving(tttPlayer: TTTPlayer, val state: State): Action(tttPlayer) {
            var cancelTask = createCancelTask()
            private val startedAt: Instant = Instant.now()
            val duration get() = Duration.between(startedAt, Instant.now()).toMillis().toDouble() / 1000

            fun createCancelTask() = plugin.server.scheduler.runTaskLater(plugin, fun() {
                cancel()
            }, 10)

            fun cancel() {
                cancelTask.cancel()
                state.action = Canceled(tttPlayer)
            }

            init {
                GameManager.world.playSound(
                    tttPlayer.player.location,
                    Resourcepack.Sounds.Item.Defibrillator.use,
                    SoundCategory.PLAYERS,
                    1F,
                    1F
                )

                state.bossBar.color = BarColor.GREEN
                state.bossBar.addPlayer(tttPlayer.player)
            }

            override fun reset() = cancelTask.cancel()
        }

        class Canceled(tttPlayer: TTTPlayer): Action(tttPlayer) {
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

                task = plugin.server.scheduler.runTaskTimer(plugin, fun() {
                    val state = isc.get(tttPlayer) ?: return@runTaskTimer

                    if (switches == SWITCHES_COUNT) {
                        task.cancel()
                        state.action = null
                        state.bossBar.removePlayer(tttPlayer.player)
                    } else {
                        state.bossBar.progress = 1.0
                        state.bossBar.color = if (switches % 2 == 0) BarColor.RED else BarColor.WHITE
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

    class State: IState {
        var action: Action? = null
        var bossBar = plugin.server.createBossBar(
            "${ChatColor.BOLD}Defibrillator",
            BarColor.GREEN,
            BarStyle.SOLID
        )

        fun reset(tttPlayer: TTTPlayer) {
            bossBar.removePlayer(tttPlayer.player)
            action?.reset()
            stopSound()
        }
    }
}

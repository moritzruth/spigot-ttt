package de.moritzruth.spigot_ttt.game.classes.impl

import de.moritzruth.spigot_ttt.game.GameEndEvent
import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

object Ninja: TTTClass(
    "Ninja",
    ChatColor.LIGHT_PURPLE
) {
    private val isc = InversedStateContainer(State::class)

    override fun onInit(tttPlayer: TTTPlayer) {
        tttPlayer.player.allowFlight = true
        tttPlayer.player.addPotionEffect(PotionEffect(
            PotionEffectType.JUMP,
            1000000,
            2,
            false,
            false
        ))
    }

    override val listener = object : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
            val tttPlayer = TTTPlayer.of(event.player) ?: return

            if (tttPlayer.tttClass == Ninja) {
                val state = isc.getOrCreate(tttPlayer)

                val current = tttPlayer.player.velocity
                tttPlayer.player.velocity = Vector(current.x * 3, 0.8, current.z * 3)
                state.jumpsRemaining -= 1

                if (state.jumpsRemaining == 0) {
                    tttPlayer.player.allowFlight = false

                    state.checkOnGroundTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
                        if (tttPlayer.player.isOnGround) {
                            state.jumpsRemaining = 2
                            tttPlayer.player.allowFlight = true
                            state.reset()
                        }
                    }, 1, 1)
                }

                event.isCancelled = true
            }
        }

        @EventHandler(ignoreCancelled = true)
        fun onTTTPlayerDamage(event: TTTPlayerDamageEvent) {
            if (event.tttPlayer.tttClass == Ninja) {
                if (event.deathReason == DeathReason.FALL) event.damage = 0.0
            }
        }

        @EventHandler(ignoreCancelled = true)
        fun onTTTPlayerDeath(event: TTTPlayerDeathEvent) {
            isc.get(event.tttPlayer)?.reset()
            isc.remove(event.tttPlayer)
        }

        @EventHandler
        fun onGameEnd(event: GameEndEvent) {
            isc.forEveryState { state, tttPlayer ->
                state.reset()
            }
        }
    }

    class State: IState {
        var jumpsRemaining = 2
        var checkOnGroundTask: BukkitTask? = null

        fun reset() {
            checkOnGroundTask?.cancel()
            checkOnGroundTask = null
        }
    }
}

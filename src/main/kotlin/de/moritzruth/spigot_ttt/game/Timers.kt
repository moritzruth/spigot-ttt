package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.Settings
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import org.bukkit.Sound
import org.bukkit.scheduler.BukkitTask

object Timers {
    private var bukkitTask: BukkitTask? = null
    var remainingSeconds: Int = 0

    private val isCountdownRunning get() = bukkitTask != null

    fun cancelCurrentTask() {
        bukkitTask?.cancel()
        bukkitTask = null
    }

    fun startPreparingPhaseTimer(onFinished: () -> Unit) {
        runCountdown(Settings.preparingPhaseDuration, onFinished) { remainingSeconds ->
            when (remainingSeconds) {
                in 1..5, 10, 30 -> {
                    playTimerSound()
                    GameMessenger.remainingPreparingPhaseTime(remainingSeconds)
                }
            }
        }
    }

    fun startCombatPhaseTimer(onFinished: () -> Unit) {
        runCountdown(Settings.combatPhaseDuration, onFinished) { remainingSeconds ->
            if (remainingSeconds % 60 == 0) {
                playTimerSound()
                GameMessenger.remainingRoundTime(remainingSeconds / 60)
            }
        }
    }

    fun startOverPhaseTimer(onFinished: () -> Unit) {
        runCountdown(Settings.overPhaseDuration, onFinished) {}
    }

    fun playTimerSound() {
        PlayerManager.tttPlayers.forEach { it.player.playSound(it.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f) }
    }

    private fun runCountdown(durationSeconds: Int, onFinished: () -> Unit, onSecond: (remainingSeconds: Int) -> Unit) {
        if (isCountdownRunning) {
            throw IllegalStateException("Only one countdown can be active at a time")
        }

        remainingSeconds = durationSeconds

        bukkitTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            ScoreboardHelper.forEveryScoreboard { it.updatePhaseAndTime() }
            if (remainingSeconds == 0) {
                cancelCurrentTask()
                onFinished()
            } else {
                onSecond(remainingSeconds)
                remainingSeconds -= 1
            }
        }, 0, secondsToTicks(1).toLong())
    }
}

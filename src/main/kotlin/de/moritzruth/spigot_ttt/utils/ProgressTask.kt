package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import java.time.Instant
import kotlin.math.min

data class TickData(
    val elapsedSeconds: Double,
    val progress: Double,
    val isComplete: Boolean,
    val trueProgress: Double
)

/**
 * @param duration Duration in seconds
 */
fun startProgressTask(duration: Double, startAt: Double = 0.0, onTick: (data: TickData) -> Unit): BukkitTask {
    val startedAt = Instant.now()

    var task: BukkitTask? = null
    task = plugin.server.scheduler.runTaskTimer(plugin, fun() {
        val elapsedSeconds: Double = Duration.between(startedAt, Instant.now()).toMillis().toDouble() / 1000
        val progress: Double = (elapsedSeconds / duration) + startAt
        val data = TickData(
            elapsedSeconds = elapsedSeconds,
            progress = min(a = progress, b = 1.0),
            trueProgress = progress,
            isComplete = progress >= 1.0
        )

        if (data.isComplete) {
            task?.cancel()
        }

        onTick(data)

    }, 0, 1)
    return task
}
fun TTTItem.Instance.startExpProgressTask(
    duration: Double,
    startAt: Double = 0.0,
    onFinish: () -> Unit
) = startProgressTask(duration, startAt) { data ->
    val exp = if (data.isComplete) {
        onFinish()
        0F
    } else data.progress.toFloat()
    if (isSelected) carrier!!.player.exp = exp
}

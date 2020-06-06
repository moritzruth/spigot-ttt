package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.plugin
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.scheduler.BukkitTask
import java.time.Instant
import kotlin.math.roundToInt

fun startItemDamageProgress(itemStack: ItemStack, duration: Double, startProgress: Double = 0.0, fromRight: Boolean = false, onFinish: () -> Unit): BukkitTask {
    val startedAt = Instant.now().toEpochMilli()

    lateinit var task: BukkitTask

    task = plugin.server.scheduler.runTaskTimer(plugin, fun() {
        val secondsElapsed = (Instant.now().toEpochMilli() - startedAt) / 1000.0
        val progress = secondsElapsed / duration + startProgress

        val maxDurability = getMaxDurability(itemStack.type)

        val damageMeta = itemStack.itemMeta!! as Damageable

        if (fromRight) {
            damageMeta.damage = (maxDurability * progress).roundToInt()
        } else {
            damageMeta.damage = maxDurability - (maxDurability * progress).roundToInt()
        }

        itemStack.itemMeta = damageMeta as ItemMeta

        if (progress >= 1) {
            task.cancel()
            onFinish()
        }
    }, 0, 1)

    return task
}

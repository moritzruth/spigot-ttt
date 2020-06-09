package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.plugin

fun nextTick(fn: () -> Unit) {
    plugin.server.scheduler.runTask(plugin, fn)
}

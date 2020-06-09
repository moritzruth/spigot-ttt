package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.plugin
import org.bukkit.event.Event

fun <T: Event> T.call(): T = this.also { plugin.server.pluginManager.callEvent(this) }

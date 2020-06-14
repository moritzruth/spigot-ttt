package de.moritzruth.spigot_ttt.game.classes.impl

import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.items.impl.Radar
import org.bukkit.ChatColor

object Oracle: TTTClass(
    "Oracle",
    ChatColor.DARK_AQUA,
    setOf(Radar)
)

package de.moritzruth.spigot_ttt.game.classes.impl

import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.items.impl.SecondChance
import org.bukkit.ChatColor

object Gambler: TTTClass(
    "Gambler",
    ChatColor.YELLOW,
    setOf(SecondChance)
)

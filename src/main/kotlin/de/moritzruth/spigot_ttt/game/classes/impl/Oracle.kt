package de.moritzruth.spigot_ttt.game.classes.impl

import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.classes.TTTClassCompanion
import de.moritzruth.spigot_ttt.game.items.impl.Radar
import org.bukkit.ChatColor

class Oracle: TTTClass() {
    companion object: TTTClassCompanion(
        "Oracle",
        ChatColor.DARK_AQUA,
        Oracle::class,
        setOf(Radar)
    )
}

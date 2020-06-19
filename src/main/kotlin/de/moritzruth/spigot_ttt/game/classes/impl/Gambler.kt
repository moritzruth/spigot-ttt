package de.moritzruth.spigot_ttt.game.classes.impl

import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.classes.TTTClassCompanion
import de.moritzruth.spigot_ttt.game.items.impl.SecondChance
import org.bukkit.ChatColor

class Gambler: TTTClass() {
    companion object: TTTClassCompanion(
        "Gambler",
        ChatColor.YELLOW,
        Gambler::class,
        setOf(SecondChance)
    )
}

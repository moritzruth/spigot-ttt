package de.moritzruth.spigot_ttt.game.classes

import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.ChatColor

abstract class TTTClass {
    lateinit var tttPlayer: TTTPlayer

    open fun init() {}
    open fun reset() {}

    object None: TTTClassCompanion(
        "Keine",
        ChatColor.GRAY,
        Instance::class
    ) {
        class Instance: TTTClass()
    }
}

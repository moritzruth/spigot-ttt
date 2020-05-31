package de.moritzruth.spigot_ttt

import de.moritzruth.spigot_ttt.game.AbortCommand
import de.moritzruth.spigot_ttt.game.StartCommand
import de.moritzruth.spigot_ttt.items.AddItemSpawnCommand

object CommandManager {
    fun initializeCommands() {
        StartCommand()
        AbortCommand()
        AddItemSpawnCommand()
    }
}

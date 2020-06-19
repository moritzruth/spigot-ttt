package de.moritzruth.spigot_ttt

import de.moritzruth.spigot_ttt.game.AbortCommand
import de.moritzruth.spigot_ttt.game.InfoCommand
import de.moritzruth.spigot_ttt.game.ReviveCommand
import de.moritzruth.spigot_ttt.game.StartCommand
import de.moritzruth.spigot_ttt.game.items.AddItemSpawnCommand
import de.moritzruth.spigot_ttt.worlds.WorldCommand

object CommandManager {
    fun initializeCommands() {
        StartCommand()
        AbortCommand()
        AddItemSpawnCommand()
        ReviveCommand()
        ResourcepackCommand()
        ReloadTTTConfigCommand()
        InfoCommand()
        WorldCommand()
    }
}

package de.moritzruth.spigot_ttt

import de.moritzruth.spigot_ttt.game.AbortCommand
import de.moritzruth.spigot_ttt.game.InfoCommand
import de.moritzruth.spigot_ttt.game.ReviveCommand
import de.moritzruth.spigot_ttt.game.StartCommand
import de.moritzruth.spigot_ttt.game.worlds.AddSpawnLocationCommand

object CommandManager {
    fun initializeCommands() {
        StartCommand()
        AbortCommand()
        AddSpawnLocationCommand()
        ReviveCommand()
        ResourcepackCommand()
        ReloadTTTConfigCommand()
        InfoCommand()
    }
}

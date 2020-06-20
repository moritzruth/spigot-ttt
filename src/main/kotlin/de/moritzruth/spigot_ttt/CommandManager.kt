package de.moritzruth.spigot_ttt

import de.moritzruth.spigot_ttt.game.AbortCommand
import de.moritzruth.spigot_ttt.game.InfoCommand
import de.moritzruth.spigot_ttt.game.ReviveCommand
import de.moritzruth.spigot_ttt.game.StartCommand
import de.moritzruth.spigot_ttt.game.worlds.AddSpawnLocationCommand
import de.moritzruth.spigot_ttt.game.worlds.VotingCommand

object CommandManager {
    fun initializeCommands() {
        StartCommand()
        AbortCommand()
        AddSpawnLocationCommand()
        ReviveCommand()
        ResourcepackCommand()
        ReloadTTTConfigCommand()
        InfoCommand()
        VotingCommand()
    }
}

package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.COMMAND_RESPONSE_PREFIX
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.EmptyTabCompleter
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class AbortCommand: CommandExecutor {
    init {
        plugin.getCommand("abort")?.let {
            it.setExecutor(this)
            it.tabCompleter = EmptyTabCompleter
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (GameManager.phase == null) {
            val tttWorld = GameManager.tttWorld
            if (tttWorld == null)
                sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Zurzeit läuft kein Spiel.")
            else tttWorld.unload()
        } else GameManager.abortGame(true)

        return true
    }
}

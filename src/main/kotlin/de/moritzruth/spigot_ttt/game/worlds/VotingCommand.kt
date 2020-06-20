package de.moritzruth.spigot_ttt.game.worlds

import de.moritzruth.spigot_ttt.COMMAND_RESPONSE_PREFIX
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GamePhase
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.EmptyTabCompleter
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class VotingCommand: CommandExecutor {
    init {
        val command = plugin.getCommand("voting")!!
        command.setExecutor(this)
        command.tabCompleter = EmptyTabCompleter
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.count() == 1 && args[0].equals("cancel", true)) {
            val voting = MapVoting.current
            if (voting == null) {
                sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Zurzeit läuft kein Map-Voting.")
            } else {
                voting.cancel()
            }

            return true
        }

        if (MapVoting.current == null) {
            if (GameManager.phase == null || GameManager.phase == GamePhase.OVER) {
                MapVoting.start()
            } else {
                sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Du kannst das Map-Voting nicht starten, " +
                        "während das Spiel läuft.")
            }
        } else {
            sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Es läuft bereits ein Map-Voting.")
        }

        return true
    }
}

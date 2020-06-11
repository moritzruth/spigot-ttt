package de.moritzruth.spigot_ttt

import de.moritzruth.spigot_ttt.utils.EmptyTabCompleter
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ResourcepackCommand: CommandExecutor {
    init {
        plugin.getCommand("resourcepack")!!.also {
            it.setExecutor(this)
            it.tabCompleter = EmptyTabCompleter
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Dieser Befehl kan nur als Spieler verwendet werden.")
        } else {
            sender.setResourcePack(Resourcepack.url)
        }

        return true
    }
}

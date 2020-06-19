package de.moritzruth.spigot_ttt.worlds

import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.createTabCompleter
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class WorldCommand: CommandExecutor {
    init {
        val command = plugin.getCommand("world")!!
        command.setExecutor(this)
        command.tabCompleter = createTabCompleter { _, index, args ->
            return@createTabCompleter when(index) {
                0 -> listOf("load", "save", "join", "list")
                1 -> when(args[0].toLowerCase()) {
                    "load" -> WorldManager.sourceWorlds.map { it.name }
                    "save", "join" -> WorldManager.tttWorlds.map { it.id.toString() }
                    else -> null
                }
                else -> null
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        TODO("Not yet implemented")
    }
}

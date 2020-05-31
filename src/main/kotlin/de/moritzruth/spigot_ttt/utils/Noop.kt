package de.moritzruth.spigot_ttt.utils

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

fun noop() {}

class NoOpTabCompleter: TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ) = mutableListOf<String>()
}

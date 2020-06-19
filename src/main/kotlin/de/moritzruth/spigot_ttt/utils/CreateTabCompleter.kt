package de.moritzruth.spigot_ttt.utils

import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

fun createTabCompleter(fn: (sender: CommandSender, index: Int) -> List<String>?) =
    createTabCompleter { sender, index, _ -> fn(sender, index) }

fun createTabCompleter(fn: (sender: CommandSender, index: Int, args: List<String>) -> List<String>?) =
    TabCompleter { sender, _, _, args ->
        val index = args.count()

        val completions =
            if (index == 0) emptyList()
            else fn(sender, index, args.toList()) ?: emptyList()

        completions.filter { it.startsWith(args.last(), true) }
    }

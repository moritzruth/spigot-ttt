package de.moritzruth.spigot_ttt.utils

import org.bukkit.ChatColor

fun surroundWithGraySquareBrackets(string: String) =
    "${ChatColor.RESET}${ChatColor.GRAY}[${ChatColor.RESET}$string${ChatColor.GRAY}]${ChatColor.RESET}"

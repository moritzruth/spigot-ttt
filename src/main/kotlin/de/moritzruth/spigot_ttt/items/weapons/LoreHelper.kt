package de.moritzruth.spigot_ttt.items.weapons

import org.bukkit.ChatColor

object LoreHelper {
    fun damage(value: Double?): String {
        if (value == null) {
            return "${ChatColor.GREEN}∞"
        }

        if (value <= 0) {
            throw IllegalArgumentException("damage must be higher than 0")
        }

        return "${ChatColor.RED}$value"
    }

    fun uses(value: Int?) = when {
        value == null -> "${ChatColor.GREEN}∞"
        value == 1 -> "${ChatColor.RED}1"
        value <= 5 -> "${ChatColor.YELLOW}$value"
        else -> "${ChatColor.GREEN}${value}"
    }

    fun cooldown(value: Double) = when {
        value <= 1 -> "${ChatColor.GREEN}${value}s"
        value > 3 -> "${ChatColor.RED}${value}s"
        else -> "${ChatColor.GREEN}${value}s"
    }
}

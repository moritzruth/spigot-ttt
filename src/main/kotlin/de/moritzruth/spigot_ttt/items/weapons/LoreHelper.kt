package de.moritzruth.spigot_ttt.items.weapons

import org.bukkit.ChatColor

object LoreHelper {
    fun damage(damage: Double?): String {
        if (damage == null) {
            return "${ChatColor.GREEN}∞"
        }

        if (damage <= 0) {
            throw IllegalArgumentException("damage must be higher than 0")
        }

        return "${ChatColor.RED}$damage"
    }

    fun magazineSize(maxUses: Int?) = when {
        maxUses == null -> "${ChatColor.GREEN}∞"
        maxUses == 1 -> "${ChatColor.RED}1"
        maxUses <= 5 -> "${ChatColor.YELLOW}$maxUses"
        else -> "${ChatColor.GREEN}${maxUses}"
    }

    fun cooldown(cooldown: Double) = when {
        cooldown <= 1 -> "${ChatColor.GREEN}${cooldown}s"
        cooldown > 3 -> "${ChatColor.RED}${cooldown}s"
        else -> "${ChatColor.GREEN}${cooldown}s"
    }
}

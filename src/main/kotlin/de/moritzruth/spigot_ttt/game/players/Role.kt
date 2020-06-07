package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.ResourcePack
import org.bukkit.ChatColor
import org.bukkit.Material
import java.util.*

enum class Role(
    val chatColor: ChatColor,
    val displayName: String,
    val iconItemMaterial: Material,
    val canOwnCredits: Boolean = false
) {
    INNOCENT(ChatColor.GREEN, "Innocent", ResourcePack.Items.innocent),
    DETECTIVE(ChatColor.YELLOW, "Detective", ResourcePack.Items.detective, true),
    TRAITOR(ChatColor.RED, "Traitor", ResourcePack.Items.traitor, true),
    JACKAL(ChatColor.AQUA, "Jackal", ResourcePack.Items.jackal, true),
    SIDEKICK(ChatColor.AQUA, "Sidekick", ResourcePack.Items.sidekick, false);

    val coloredDisplayName = "$chatColor$displayName${ChatColor.RESET}"

    val position by lazy { values().indexOf(this) }
    val group by lazy { RoleGroup.getGroupOf(this) }
}

fun roles(role: Role, vararg moreRoles: Role): EnumSet<Role> = EnumSet.of(role, *moreRoles)

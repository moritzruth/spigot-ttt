package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.Resourcepack
import org.bukkit.ChatColor
import org.bukkit.Material
import java.util.*

enum class Role(
    val chatColor: ChatColor,
    val displayName: String,
    val iconItemMaterial: Material,
    val canOwnCredits: Boolean = false
) {
    INNOCENT(ChatColor.GREEN, "Innocent", Resourcepack.Items.innocent),
    DETECTIVE(ChatColor.YELLOW, "Detective", Resourcepack.Items.detective, true),
    TRAITOR(ChatColor.RED, "Traitor", Resourcepack.Items.traitor, true),
    JACKAL(ChatColor.AQUA, "Jackal", Resourcepack.Items.jackal, true),
    SIDEKICK(ChatColor.AQUA, "Sidekick", Resourcepack.Items.sidekick, false);

    val coloredDisplayName = "$chatColor$displayName${ChatColor.RESET}"

    val position by lazy { values().indexOf(this) }
    val group by lazy { RoleGroup.getGroupOf(this) }
}

fun roles(role: Role, vararg moreRoles: Role): EnumSet<Role> = EnumSet.of(role, *moreRoles)

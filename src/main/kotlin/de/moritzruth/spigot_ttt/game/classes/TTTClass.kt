package de.moritzruth.spigot_ttt.game.classes

import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.ChatColor
import org.bukkit.event.Listener

abstract class TTTClass(
    val displayName: String,
    val chatColor: ChatColor,
    val defaultItems: Set<TTTItem> = emptySet()
) {
    val coloredDisplayName = "$chatColor$displayName"

    open val listener: Listener? = null

    open fun onInit(tttPlayer: TTTPlayer) {}

    object None: TTTClass(
        "Keine",
        ChatColor.GRAY
    )
}

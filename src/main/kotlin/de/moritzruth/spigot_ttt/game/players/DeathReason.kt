package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.items.TTTItem

sealed class DeathReason(val displayText: String) {
    object DISCONNECTED: DeathReason("Verbindung unterbrochen")
    object SUICIDE: DeathReason("Suizid")
    class Item(val item: TTTItem): DeathReason("Getötet mit: ${item.displayName}")
}

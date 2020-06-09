package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.game.items.TTTItem

sealed class DeathReason(val displayText: String) {
    object DISCONNECTED: DeathReason("Verbindung unterbrochen")
    object SUICIDE: DeathReason("Suizid")
    object FALL: DeathReason("Fallschaden")
    object EXPLOSION: DeathReason("Explosion")
    object DROWNED: DeathReason("Ertrunken")
    object FIRE: DeathReason("Verbrannt")
    object POISON: DeathReason("Vergiftet")
    class Item(val item: TTTItem): DeathReason("Getötet mit: ${item.itemStack.itemMeta!!.displayName}")
}

package de.moritzruth.spigot_ttt.items

import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.*

interface SelectableItem: TTTItem {
    fun onSelect(tttPlayer: TTTPlayer)
    fun onDeselect(tttPlayer: TTTPlayer)
}

interface BuyableItem: TTTItem {
    val buyableBy: EnumSet<TTTPlayer.Role>
    val price: Int
}

interface TTTItem {
    val displayName: String
    val listener: Listener
    val itemStack: ItemStack
    val spawning: Boolean
    val type: Type

    enum class Type(val maxItemsOfTypeInInventory: Int?) {
        MELEE(1),
        PISTOL_LIKE(1),
        HEAVY_WEAPON(1),
        SPECIAL(null);

        val position by lazy { values().indexOf(this) }
    }
}

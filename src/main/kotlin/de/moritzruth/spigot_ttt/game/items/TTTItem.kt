package de.moritzruth.spigot_ttt.game.items

import com.comphenix.protocol.events.PacketListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.ChatColor
import org.bukkit.entity.Item
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.*

interface Selectable {
    fun onSelect(tttPlayer: TTTPlayer)
    fun onDeselect(tttPlayer: TTTPlayer)
}

interface DropHandler {
    fun onDrop(tttPlayer: TTTPlayer, itemEntity: Item): Boolean
    fun onPickup(tttPlayer: TTTPlayer, itemEntity: Item)
}

interface Buyable {
    val buyableBy: EnumSet<Role>
    val price: Int
    val buyLimit: Int?
}

val PASSIVE = "${ChatColor.RESET}${ChatColor.RED}(Passiv)"

// Marker
interface Spawning

interface TTTItem {
    val listener: Listener? get() = null
    val packetListener: PacketListener? get() = null
    val itemStack: ItemStack
    val type: Type

    fun onOwn(tttPlayer: TTTPlayer) {}
    fun onRemove(tttPlayer: TTTPlayer) {}

    enum class Type(val maxItemsOfTypeInInventory: Int?) {
        MELEE(1),
        PISTOL_LIKE(1),
        HEAVY_WEAPON(1),
        SPECIAL(null);
    }
}

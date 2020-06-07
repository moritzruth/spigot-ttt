package de.moritzruth.spigot_ttt.items

import com.comphenix.protocol.events.PacketListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*

interface Selectable {
    fun onSelect(tttPlayer: TTTPlayer)
    fun onDeselect(tttPlayer: TTTPlayer)
}

interface DropHandler {
    fun onDrop(tttPlayer: TTTPlayer, itemEntity: Item)
    fun onPickup(tttPlayer: TTTPlayer, itemEntity: Item)
}

interface Buyable {
    val buyableBy: EnumSet<Role>
    val price: Int
}

// Marker
interface Spawning

interface TTTItem {
    val listener: Listener? get() = null
    val packetListener: PacketListener? get() = null
    val itemStack: ItemStack
    val type: Type

    enum class Type(val maxItemsOfTypeInInventory: Int?) {
        MELEE(1),
        PISTOL_LIKE(1),
        HEAVY_WEAPON(1),
        SPECIAL(null);
    }
}

fun PlayerInteractEvent.isRelevant(tttItem: TTTItem): Boolean = item?.type == tttItem.itemStack.type
fun EntityDamageByEntityEvent.isRelevant(tttItem: TTTItem): Boolean = damager is Player && entity is Player &&
            (damager as Player).inventory.itemInMainHand.type == tttItem.itemStack.type

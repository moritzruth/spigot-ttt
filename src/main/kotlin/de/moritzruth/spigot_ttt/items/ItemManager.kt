package de.moritzruth.spigot_ttt.items

import com.comphenix.protocol.ProtocolLibrary
import com.connorlinfoot.actionbarapi.ActionBarAPI
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.IState
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.items.impl.*
import de.moritzruth.spigot_ttt.items.weapons.guns.impl.*
import de.moritzruth.spigot_ttt.items.weapons.impl.BaseballBat
import de.moritzruth.spigot_ttt.items.weapons.impl.Knife
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack

object ItemManager {
    val ITEMS: Set<TTTItem> = setOf(
        Pistol,
        Knife, Glock, Deagle, Shotgun, SidekickDeagle,
        BaseballBat,
        CloakingDevice, Rifle,
        EnderPearl, Radar, HealingPotion, Fireball,
        Teleporter
    )

    val droppedItemStates = mutableMapOf<Int, IState>()

    fun registerListeners() {
        plugin.server.pluginManager.registerEvents(listener, plugin)

        for (item in ITEMS) {
            if (item.listener != null) plugin.server.pluginManager.registerEvents(item.listener!!, plugin)
            if (item.packetListener != null) ProtocolLibrary.getProtocolManager().addPacketListener(item.packetListener!!)
        }
    }

    private fun getItemByMaterial(material: Material) = ITEMS.find { tttItem -> material === tttItem.itemStack.type }
    fun getItemByItemStack(itemStack: ItemStack) = getItemByMaterial(itemStack.type)

    fun reset() {
        droppedItemStates.clear()
        GameManager.world.getEntitiesByClass(Item::class.java).forEach {
            it.remove()
        }
    }

    val listener = object : Listener {
        @EventHandler
        fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return
            val itemStack = event.player.inventory.getItem(event.newSlot)

            tttPlayer.itemInHand =
                if (itemStack == null || itemStack.type === Material.AIR) null
                else getItemByItemStack(itemStack)
        }

        @EventHandler
        fun onPlayerDropItem(event: PlayerDropItemEvent) {
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return
            val tttItem = getItemByItemStack(event.itemDrop.itemStack) ?: return

            if (tttItem.type != TTTItem.Type.SPECIAL) {
                if (tttItem is DropHandler) {
                    if (!tttItem.onDrop(tttPlayer, event.itemDrop)) {
                        event.isCancelled = true
                        return
                    }
                }

                plugin.server.scheduler.runTask(plugin, fun() {
                    tttPlayer.updateItemInHand()
                })
            } else {
                ActionBarAPI.sendActionBar(event.player, "${ChatColor.RED}Du kannst dieses Item nicht droppen")
                event.isCancelled = true
            }
        }

        @EventHandler
        fun onItemDespawn(event: ItemDespawnEvent) {
            if (getItemByItemStack(event.entity.itemStack) != null) {
                event.entity.ticksLived = 1
                event.isCancelled = true
            }
        }

        @EventHandler
        fun onEntityPickupItem(event: EntityPickupItemEvent) {
            if (event.entity !is Player) {
                return
            }

            val player = event.entity as Player
            val tttPlayer = PlayerManager.getTTTPlayer(player) ?: return

            val tttItem = getItemByItemStack(event.item.itemStack)

            if (tttItem != null) {
                if (runCatching { tttPlayer.checkAddItemPreconditions(tttItem) }.isSuccess) {
                    plugin.server.scheduler.runTask(plugin, fun() {
                        tttPlayer.updateItemInHand()
                    })

                    if (tttItem is DropHandler) {
                        tttItem.onPickup(tttPlayer, event.item)
                    }

                    return
                }
            }

            event.isCancelled = true
        }
    }
}

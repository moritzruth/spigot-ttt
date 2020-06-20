package de.moritzruth.spigot_ttt.game.items

import com.codingforcookies.armorequip.ArmorEquipEvent
import de.moritzruth.spigot_ttt.game.GameListener
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.items.impl.*
import de.moritzruth.spigot_ttt.game.items.impl.weapons.BaseballBat
import de.moritzruth.spigot_ttt.game.items.impl.weapons.Fireball
import de.moritzruth.spigot_ttt.game.items.impl.weapons.Knife
import de.moritzruth.spigot_ttt.game.items.impl.weapons.guns.*
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayerDeathInPreparingEvent
import de.moritzruth.spigot_ttt.game.players.TTTPlayerTrueDeathEvent
import de.moritzruth.spigot_ttt.utils.isLeftClick
import de.moritzruth.spigot_ttt.utils.isRightClick
import de.moritzruth.spigot_ttt.utils.nextTick
import de.moritzruth.spigot_ttt.utils.sendActionBarMessage
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack

object ItemManager {
    val ITEMS: Set<TTTItem<*>> = setOf(
        Deagle, Glock, Pistol, Rifle, SidekickDeagle, BaseballBat, Knife, CloakingDevice, Defibrillator,
        EnderPearl, FakeCorpse, Fireball, HealingPotion, MartyrdomGrenade, Radar, SecondChance, Teleporter,
        Shotgun, Radar, SecondChance, BoomBody, TreeGun
    )

    val listeners get () = ITEMS.mapNotNull { it.listener }.plus(listener)
    val packetListeners get () = ITEMS.mapNotNull { it.packetListener }

    private fun getTTTItemByMaterial(material: Material) = ITEMS.find { tttItem -> material == tttItem.material }
    fun getTTTItemByItemStack(itemStack: ItemStack) = getTTTItemByMaterial(itemStack.type)
    fun getInstanceByItemStack(itemStack: ItemStack) = getTTTItemByItemStack(itemStack)?.getInstance(itemStack)

    fun dropItem(location: Location, tttItem: TTTItem<*>) {
        val instance = tttItem.createInstance()
        GameManager.world.dropItem(location, instance.createItemStack())
    }

    fun reset() {
        GameManager.world.getEntitiesByClass(Item::class.java).forEach(Item::remove)
        ITEMS.forEach(TTTItem<*>::reset)
    }

    val listener = object : GameListener() {
        @EventHandler(priority = EventPriority.LOW)
        fun onInventoryClick(event: InventoryClickEvent) = handle(event) { tttPlayer ->
            if (event.click === ClickType.CREATIVE || event.clickedInventory?.holder != event.whoClicked) return@handle

            if (event.slot in 0..8) {
                // is in hotbar
                when(event.action) {
                    InventoryAction.PICKUP_ALL,
                    InventoryAction.PICKUP_HALF,
                    InventoryAction.PICKUP_ONE -> {
                        event.currentItem?.also { itemStack -> getInstanceByItemStack(itemStack)?.isSelected = false }
                    }
                    InventoryAction.PLACE_ALL,
                    InventoryAction.PLACE_SOME,
                    InventoryAction.PLACE_ONE -> {
                        nextTick {
                            if (event.slot == tttPlayer.player.inventory.heldItemSlot) {
                                tttPlayer.player.inventory.getItem(event.slot)?.also { itemStack ->
                                    getInstanceByItemStack(itemStack)?.isSelected = true
                                }
                            }
                        }
                    }
                    else -> event.isCancelled = true
                }
            }
        }

        @EventHandler
        fun onArmorEquip(event: ArmorEquipEvent) {
            if (event.player.gameMode != GameMode.CREATIVE) event.isCancelled = true
        }

        @EventHandler(ignoreCancelled = true)
        fun onPlayerInteract(event: PlayerInteractEvent) = handle(event) { tttPlayer ->
            if (tttPlayer.ignoreNextInteract) {
                tttPlayer.ignoreNextInteract = false
                event.isCancelled = true
                return@handle
            }

            val instance = event.item?.let { getInstanceByItemStack(it) } ?: return@handle

            val clickEvent = ClickEvent()
            if (event.action.isLeftClick) instance.onLeftClick(clickEvent)
            else if (event.action.isRightClick) instance.onRightClick(clickEvent)
            event.isCancelled = clickEvent.isCancelled
        }

        @EventHandler(ignoreCancelled = true)
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            val damager = event.damager
            if (damager is Player) {
                TTTPlayer.of(damager) ?: return
                val item = damager.inventory.itemInMainHand
                if (item.type != Material.AIR) {
                    val tttItem = getTTTItemByItemStack(item) ?: return
                    event.isCancelled = tttItem.disableDamage
                }
            }
        }

        @EventHandler
        fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) = handle(event) { _ ->
            val instance = event.offHandItem?.let { getInstanceByItemStack(it) } ?: return@handle
            instance.onHandSwap()
            event.isCancelled = true
        }

        @EventHandler
        fun onPlayerItemHeld(event: PlayerItemHeldEvent) = handle(event) { tttPlayer ->
            tttPlayer.player.inventory.getItem(event.previousSlot)
                ?.also { itemStack -> getInstanceByItemStack(itemStack)?.isSelected = false }

            tttPlayer.player.inventory.getItem(event.newSlot)
                ?.also { itemStack -> getInstanceByItemStack(itemStack)?.isSelected = true }
        }

        @EventHandler
        fun onPlayerDropItem(event: PlayerDropItemEvent) = handle(event) { tttPlayer ->
            val instance = getInstanceByItemStack(event.itemDrop.itemStack) ?: return@handle

            val notDroppableReason = instance.notDroppableReason
            if (notDroppableReason == null) {
                instance.carrier = null
            } else {
                tttPlayer.player.sendActionBarMessage(notDroppableReason)
                event.isCancelled = true
            }
        }

        @EventHandler
        fun onEntityPickupItem(event: EntityPickupItemEvent) {
            val player = event.entity
            if (player !is Player) return

            val tttPlayer = TTTPlayer.of(player) ?: return
            val instance = getInstanceByItemStack(event.item.itemStack)

            if (instance != null) {
                if (runCatching { tttPlayer.checkAddItemPreconditions(instance.tttItem) }.isSuccess) {
                    instance.carrier = tttPlayer
                    return
                }
            }

            event.isCancelled = true
        }

        @EventHandler
        fun onItemDespawn(event: ItemDespawnEvent) {
            if (getTTTItemByItemStack(event.entity.itemStack) != null) {
                event.entity.ticksLived = 1
                event.isCancelled = true
            }
        }

        @EventHandler
        fun onTTTPlayerDeathInPreparing(event: TTTPlayerDeathInPreparingEvent) {
            val itemStackInHand = event.tttPlayer.player.inventory.itemInMainHand
            if (itemStackInHand.type != Material.AIR) {
                val instance = getInstanceByItemStack(itemStackInHand)

                if (
                    instance != null &&
                    !event.tttPlayer.tttClass.defaultItems.contains(instance.tttItem) &&
                    instance.notDroppableReason == null
                ) {
                    event.tttPlayer.removeItem(instance.tttItem, removeInstance = false, becauseOfDeath = true)
                    GameManager.world.dropItem(event.location, instance.createItemStack())
                }
            }
        }

        @EventHandler
        fun onTTTPlayerTrueDeath(event: TTTPlayerTrueDeathEvent) {
            val itemStackInHand = event.tttPlayer.player.inventory.itemInMainHand
            if (itemStackInHand.type != Material.AIR) {
                val instance = getInstanceByItemStack(itemStackInHand)

                if (instance != null && instance.notDroppableReason == null) {
                    event.tttPlayer.removeItem(instance.tttItem, removeInstance = false, becauseOfDeath = true)
                    GameManager.world.dropItem(event.location, instance.createItemStack())
                }
            }
        }
    }
}

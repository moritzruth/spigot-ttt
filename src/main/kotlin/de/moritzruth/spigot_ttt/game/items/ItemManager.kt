package de.moritzruth.spigot_ttt.game.items

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.items.impl.*
import de.moritzruth.spigot_ttt.game.items.impl.weapons.BaseballBat
import de.moritzruth.spigot_ttt.game.items.impl.weapons.Fireball
import de.moritzruth.spigot_ttt.game.items.impl.weapons.Knife
import de.moritzruth.spigot_ttt.game.items.impl.weapons.guns.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

object ItemManager {
    val ITEMS: Set<TTTItem<*>> = setOf(
        Deagle, Glock, Pistol, Rifle, SidekickDeagle, BaseballBat, Knife, CloakingDevice, Defibrillator,
        EnderPearl, FakeCorpse, Fireball, HealingPotion, MartyrdomGrenade, Radar, SecondChance, Teleporter,
        Shotgun, Radar, SecondChance, BoomBody, TreeGun
    )

    val listeners get () = ITEMS.flatMap { it.listeners }.plus(ItemListener)
    val packetListeners get () = ITEMS.flatMap { it.packetListeners }

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
}

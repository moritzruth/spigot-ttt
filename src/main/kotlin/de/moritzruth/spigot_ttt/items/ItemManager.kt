package de.moritzruth.spigot_ttt.items

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.items.weapons.BaseballBat
import de.moritzruth.spigot_ttt.items.weapons.Knife
import de.moritzruth.spigot_ttt.items.weapons.guns.Deagle
import de.moritzruth.spigot_ttt.items.weapons.guns.Glock
import de.moritzruth.spigot_ttt.items.weapons.guns.Pistol
import de.moritzruth.spigot_ttt.items.weapons.guns.Shotgun
import de.moritzruth.spigot_ttt.items.weapons.guns.deagle.GoldenDeagle
import de.moritzruth.spigot_ttt.items.weapons.guns.pistol.Rifle
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.ConfigurationFile
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt

object ItemManager {
    private const val CONFIG_PATH = "spawn-locations"

    private val spawnLocationsConfig = ConfigurationFile("spawnLocations")

    val items: Set<TTTItem> = setOf(
        Pistol, Knife, Glock, Deagle, Shotgun, GoldenDeagle, BaseballBat,
        CloakingDevice, Rifle,
        EnderPearl, Radar, HealingPotion
    )
    private val spawningItems = items.filter { it is Spawning }

    fun registerListeners() {
        for (item in items) {
            val listener = item.listener

            if (listener != null) {
                plugin.server.pluginManager.registerEvents(listener, plugin)
            }
        }
    }

    private fun getItemByMaterial(material: Material) = items.find { tttItem -> material === tttItem.itemStack.type }
    fun getItemByItemStack(itemStack: ItemStack) = getItemByMaterial(itemStack.type)

    private fun getSpawnLocations(): Set<Location> {
        return spawnLocationsConfig.getStringList(CONFIG_PATH).map {
            val (x, y, z) = it.split(":").map(String::toDouble)
            Location(GameManager.world, x, y, z)
        }.toSet()
    }

    private fun setSpawnLocations(spawnLocations: Set<Location>) {
        spawnLocationsConfig.set(CONFIG_PATH, spawnLocations.map {
            "${it.x}:${it.y}:${it.z}"
        })
    }

    fun spawnWeapons() {
        for (location in getSpawnLocations()) {
            GameManager.world.dropItem(location, spawningItems.random().itemStack.clone())
        }
    }

    fun removeItemEntities() {
        GameManager.world.getEntitiesByClass(Item::class.java).forEach {
            it.remove()
        }
    }

    fun addItemSpawnLocation(location: Location) {
        val spawnLocations = getSpawnLocations().toMutableSet()

        spawnLocations.add(roundLocationToHalfBlock(location))
        setSpawnLocations(spawnLocations)
        spawnLocationsConfig.save()
    }

    private fun roundLocationToHalfBlock(location: Location) = Location(location.world, roundToHalf(location.x), roundToHalf(location.y), roundToHalf(location.z))

    private fun roundToHalf(number: Double): Double = (number * 2).roundToInt() / 2.0
}

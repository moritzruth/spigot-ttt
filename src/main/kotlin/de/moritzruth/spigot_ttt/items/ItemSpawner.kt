package de.moritzruth.spigot_ttt.items

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.utils.ConfigurationFile
import de.moritzruth.spigot_ttt.utils.roundToCenter
import org.bukkit.Location

object ItemSpawner {
    private const val CONFIG_PATH = "spawn-locations"

    private val spawnLocationsConfig = ConfigurationFile("spawnLocations")
    private val spawningItems = ItemManager.ITEMS.filter { it is Spawning }

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
        var itemIterator = spawningItems.shuffled().iterator()

        for (location in getSpawnLocations()) {
            if (!itemIterator.hasNext()) {
                itemIterator = spawningItems.shuffled().iterator()
            }

            GameManager.world.dropItem(location, itemIterator.next().itemStack.clone())
        }
    }

    fun addItemSpawnLocation(location: Location) {
        val spawnLocations = getSpawnLocations().toMutableSet()

        spawnLocations.add(location.roundToCenter())
        setSpawnLocations(spawnLocations)
        spawnLocationsConfig.save()
    }
}

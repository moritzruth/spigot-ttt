package de.moritzruth.spigot_ttt.game.items

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.utils.ConfigurationFile
import de.moritzruth.spigot_ttt.utils.roundToCenter
import org.bukkit.Location
import java.util.*

object ItemSpawner {
    private const val CONFIG_PATH = "spawn-locations"

    private val spawnLocationsConfig = ConfigurationFile("spawnLocations")

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
        val spawningItems = mutableListOf<TTTItem<*>>()
        for (tttItem in ItemManager.ITEMS) {
            val count = tttItem.spawnProbability?.multiplier ?: 0
            spawningItems.addAll(Collections.nCopies(count, tttItem))
        }

        var itemsIterator = spawningItems.shuffled().iterator()
        for (location in getSpawnLocations()) {
            if (!itemsIterator.hasNext()) {
                itemsIterator = spawningItems.shuffled().iterator()
            }

            ItemManager.dropItem(location, itemsIterator.next())
        }
    }

    fun addItemSpawnLocation(location: Location) {
        val spawnLocations = getSpawnLocations().toMutableSet()

        spawnLocations.add(location.roundToCenter())
        setSpawnLocations(spawnLocations)
        spawnLocationsConfig.save()
    }
}

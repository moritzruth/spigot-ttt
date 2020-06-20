package de.moritzruth.spigot_ttt.game.worlds

import de.moritzruth.spigot_ttt.utils.getSpawnLocations
import de.moritzruth.spigot_ttt.utils.roundToCenter
import de.moritzruth.spigot_ttt.utils.setSpawnLocations
import org.bukkit.Location

class SpawnLocationsManager(private val tttWorld: TTTWorld) {
    fun getItemSpawnLocations() = tttWorld.config.getSpawnLocations("spawn-locations.items", tttWorld.world!!, false)
    private fun setItemSpawnLocations(spawnLocations: Set<Location>) =
        tttWorld.config.setSpawnLocations("spawn-locations.items", spawnLocations, false)

    fun getPlayerSpawnLocations() = tttWorld.config.getSpawnLocations("spawn-locations.players", tttWorld.world!!, true)
    private fun setPlayerSpawnLocations(spawnLocations: Set<Location>) =
        tttWorld.config.setSpawnLocations("spawn-locations.players", spawnLocations, true)

    fun addItemSpawnLocation(location: Location) {
        val spawnLocations = getItemSpawnLocations().toMutableSet()
        spawnLocations.add(location.roundToCenter())
        setItemSpawnLocations(spawnLocations)
        tttWorld.config.save()
    }

    fun addPlayerSpawnLocation(location: Location) {
        val spawnLocations = getPlayerSpawnLocations().toMutableSet()
        spawnLocations.add(location.roundToCenter())
        setPlayerSpawnLocations(spawnLocations)
        tttWorld.config.save()
    }
}

package de.moritzruth.spigot_ttt.game.items

import de.moritzruth.spigot_ttt.game.GameManager
import java.util.*

object ItemSpawner {
    fun spawnWeapons() {
        val spawningItems = mutableListOf<TTTItem<*>>()
        for (tttItem in ItemManager.ITEMS) {
            val count = tttItem.spawnProbability?.multiplier ?: 0
            spawningItems.addAll(Collections.nCopies(count, tttItem))
        }

        var itemsIterator = spawningItems.shuffled().iterator()
        for (location in GameManager.tttWorld!!.spawnLocations.getItemSpawnLocations()) {
            if (!itemsIterator.hasNext()) {
                itemsIterator = spawningItems.shuffled().iterator()
            }

            ItemManager.dropItem(location, itemsIterator.next())
        }
    }
}

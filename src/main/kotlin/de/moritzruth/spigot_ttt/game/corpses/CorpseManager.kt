package de.moritzruth.spigot_ttt.game.corpses

import org.bukkit.inventory.Inventory
import org.golde.bukkit.corpsereborn.nms.Corpses

object CorpseManager {
    private val corpses = mutableListOf<TTTCorpse>()

    fun getTTTCorpse(corpse: Corpses.CorpseData): TTTCorpse? {
        return corpses.find { it.corpse === corpse }
    }

    fun add(corpse: TTTCorpse) {
        corpses.add(corpse)
    }

    fun isCorpseInventory(inventory: Inventory) = corpses.find { it.inventory == inventory } != null

    fun destroyAll() {
        corpses.forEach(TTTCorpse::destroy)
        corpses.clear()
    }
}

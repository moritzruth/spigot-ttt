package de.moritzruth.spigot_ttt.game.corpses

import org.bukkit.entity.Entity
import org.bukkit.entity.Zombie
import org.bukkit.inventory.Inventory

object CorpseManager {
    val corpses = mutableListOf<TTTCorpse>()

    fun getTTTCorpse(entity: Entity): TTTCorpse? =
        if (entity is Zombie) corpses.find { it.entity === entity } else null

    fun add(corpse: TTTCorpse) {
        corpses.add(corpse)
    }

    fun isCorpseInventory(inventory: Inventory) = corpses.find { it.inventory == inventory } != null

    fun destroyAll() {
        corpses.toSet().forEach(TTTCorpse::destroy)
    }
}

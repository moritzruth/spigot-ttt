package de.moritzruth.spigot_ttt.game.corpses

import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.inventory.Inventory
import org.golde.bukkit.corpsereborn.nms.Corpses

object CorpseManager {
    private val corpses = mutableListOf<TTTCorpse>()

    fun getTTTCorpse(corpse: Corpses.CorpseData): TTTCorpse? {
        return corpses.find { it.corpse === corpse }
    }

    fun isCorpseInventory(inventory: Inventory) = corpses.find { it.inventory == inventory } != null

    fun spawn(tttPlayer: TTTPlayer, reason: DeathReason) {
        corpses.add(TTTCorpse(tttPlayer.player, tttPlayer.player.location, tttPlayer.role, reason))
    }

    fun destroyAll() {
        corpses.forEach(TTTCorpse::destroy)
        corpses.clear()
    }
}

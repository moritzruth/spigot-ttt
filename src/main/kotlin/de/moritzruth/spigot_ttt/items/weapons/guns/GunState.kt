package de.moritzruth.spigot_ttt.items.weapons.guns

import de.moritzruth.spigot_ttt.game.players.State
import org.bukkit.scheduler.BukkitTask

open class GunState(magazineSize: Int): State {
    var cooldownOrReloadTask: BukkitTask? = null
    var remainingShots = magazineSize
}

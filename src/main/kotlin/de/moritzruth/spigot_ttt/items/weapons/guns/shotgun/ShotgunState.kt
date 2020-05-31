package de.moritzruth.spigot_ttt.items.weapons.guns.shotgun

import de.moritzruth.spigot_ttt.items.weapons.guns.GunState
import org.bukkit.scheduler.BukkitTask

class ShotgunState: GunState(Shotgun.magazineSize) {
    var reloadUpdateTask: BukkitTask? = null
}

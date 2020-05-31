package de.moritzruth.spigot_ttt.utils

import org.bukkit.entity.Player

fun teleportPlayerToWorldSpawn(player: Player) {
    player.teleport(player.world.spawnLocation)
}

package de.moritzruth.spigot_ttt.utils

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection

fun ConfigurationSection.getSpawnLocations(path: String, world: World, withDirection: Boolean): Set<Location> =
    getStringList(path).map {
        val parts = it.split(":")
        val (x, y, z) = parts.map(String::toDouble)
        val yaw = if (withDirection) parts[3].toFloat() else 0F
        val pitch = if (withDirection) parts[4].toFloat() else 0F

        Location(world, x, y, z, yaw, pitch)
    }.toSet()

fun ConfigurationSection.setSpawnLocations(path: String, locations: Iterable<Location>, withDirection: Boolean) =
    set(path, locations.map { location ->
        "${location.x}:${location.y}:${location.z}"
            .let { if (withDirection) "${it}:${location.yaw}:${location.pitch}" else it }
    })

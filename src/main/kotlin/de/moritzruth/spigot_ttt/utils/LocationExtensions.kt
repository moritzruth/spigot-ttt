package de.moritzruth.spigot_ttt.utils

import org.bukkit.Location

fun Location.roundToCenter() =
    Location(world,
        roundToHalf(x),
        roundToHalf(y),
        roundToHalf(z)
    )

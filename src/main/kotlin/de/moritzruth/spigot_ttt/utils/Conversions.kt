package de.moritzruth.spigot_ttt.utils

fun secondsToTicks(seconds: Double) = (seconds * 20).toInt()
fun secondsToTicks(seconds: Int) = secondsToTicks(seconds.toDouble())
fun heartsToHealth(hearts: Double) = hearts * 2

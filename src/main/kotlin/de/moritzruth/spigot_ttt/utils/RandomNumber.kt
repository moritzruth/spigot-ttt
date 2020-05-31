package de.moritzruth.spigot_ttt.utils

import kotlin.math.roundToInt

fun randomNumber(min: Int = 0, max: Int): Int = (min + (Math.random() * (max - min))).roundToInt()

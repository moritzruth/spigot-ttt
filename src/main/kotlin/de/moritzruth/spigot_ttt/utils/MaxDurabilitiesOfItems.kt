package de.moritzruth.spigot_ttt.utils

import org.bukkit.Material

val maxDurabilitiesOfItems = mapOf(
        Material.WOODEN_HOE to 59,
        Material.STONE_HOE to 131,
        Material.GOLDEN_HOE to 32,
        Material.IRON_HOE to 250,
        Material.DIAMOND_HOE to 1561,
        Material.IRON_AXE to 250
)

fun getMaxDurability(material: Material) = maxDurabilitiesOfItems[material] ?: throw Exception("The maximum durability of this item is not defined")

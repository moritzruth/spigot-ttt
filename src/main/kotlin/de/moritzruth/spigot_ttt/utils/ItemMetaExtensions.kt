package de.moritzruth.spigot_ttt.utils

import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

fun ItemMeta.hideInfo(): ItemMeta {
    addItemFlags(*ItemFlag.values())
    return this
}

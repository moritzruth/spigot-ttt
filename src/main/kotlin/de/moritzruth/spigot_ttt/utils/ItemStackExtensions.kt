package de.moritzruth.spigot_ttt.utils

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.applyMeta(fn: ItemMeta.() -> Unit): ItemStack {
    itemMeta = itemMeta!!.apply(fn)
    return this
}

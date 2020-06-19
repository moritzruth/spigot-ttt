package de.moritzruth.spigot_ttt.utils

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.applyMeta(fn: ItemMeta.() -> Unit): ItemStack {
    itemMeta = itemMeta!!.apply(fn)
    return this
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> ItemStack.applyTypedMeta(fn: T.() -> Unit): ItemStack {
    itemMeta = (itemMeta as T).apply(fn) as ItemMeta
    return this
}

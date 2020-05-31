package de.moritzruth.spigot_ttt.items

import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

fun EntityDamageByEntityEvent.isRelevant(itemName: String): Boolean =
        damager is Player &&
        entity is Player &&
        (damager as Player).inventory.itemInMainHand.itemMeta?.displayName == itemName

fun PlayerInteractEvent.isRelevant(itemName: String): Boolean = item?.itemMeta?.displayName == itemName

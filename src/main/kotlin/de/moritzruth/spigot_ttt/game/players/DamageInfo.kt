package de.moritzruth.spigot_ttt.game.players

import org.bukkit.event.entity.EntityDamageEvent

data class DamageInfo(
    val damager: TTTPlayer,
    val deathReason: DeathReason,
    val expectedDamageCause: EntityDamageEvent.DamageCause = EntityDamageEvent.DamageCause.CUSTOM
)

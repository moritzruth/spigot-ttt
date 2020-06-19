package de.moritzruth.spigot_ttt.game.classes.impl

import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.classes.TTTClassCompanion
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.TTTPlayerDamageEvent
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class Stuntman: TTTClass() {
    companion object: TTTClassCompanion(
        "Stuntman",
        ChatColor.DARK_RED,
        Stuntman::class
    ) {
        val IMMUNE_DEATH_REASONS = setOf(DeathReason.FALL, DeathReason.EXPLOSION)

        override val listener = object : Listener {
            @EventHandler(ignoreCancelled = true)
            fun onEntityDamage(event: TTTPlayerDamageEvent) {
                if (event.tttPlayer.tttClass == Stuntman) {
                    if (IMMUNE_DEATH_REASONS.contains(event.deathReason)) event.damage = 0.0
                }
            }
        }
    }
}

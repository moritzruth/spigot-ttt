package de.moritzruth.spigot_ttt.game.classes.impl

import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import java.util.*

object Stuntman: TTTClass(
    "Stuntman",
    ChatColor.DARK_RED
) {
    val IMMUNE_DAMAGE_CAUSES = EnumSet.of(
        EntityDamageEvent.DamageCause.FALL,
        EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
        EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
    )!!

    override val listener = object : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onEntityDamage(event: EntityDamageEvent) {
            val entity = event.entity
            if (entity !is Player) return

            val tttPlayer = TTTPlayer.of(entity) ?: return
            if (tttPlayer.tttClass == Stuntman) {
                if (IMMUNE_DAMAGE_CAUSES.contains(event.cause)) event.damage = 0.0
            }
        }
    }
}

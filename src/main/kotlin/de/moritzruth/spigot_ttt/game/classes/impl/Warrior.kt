package de.moritzruth.spigot_ttt.game.classes.impl

import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

object Warrior: TTTClass(
    "Warrior",
    ChatColor.BLUE
) {
    override fun onInit(tttPlayer: TTTPlayer) {
        tttPlayer.walkSpeed -= 0.05F
    }

    override val listener = object : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onEntityDamage(event: EntityDamageEvent) {
            val entity = event.entity
            if (entity !is Player) return

            val tttPlayer = TTTPlayer.of(entity) ?: return
            if (tttPlayer.tttClass == Warrior) {
                event.damage *= 0.9
            }
        }
    }
}

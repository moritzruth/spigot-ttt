package de.moritzruth.spigot_ttt.game.classes.impl

import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayerDamageEvent
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object Warrior: TTTClass(
    "Warrior",
    ChatColor.BLUE
) {
    override fun onInit(tttPlayer: TTTPlayer) {
        tttPlayer.walkSpeed -= 0.05F
    }

    override val listener = object : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onEntityDamage(event: TTTPlayerDamageEvent) {
            if (event.tttPlayer.tttClass == Warrior) {
                event.damage *= 0.9
            }
        }
    }
}

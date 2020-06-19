package de.moritzruth.spigot_ttt.game.classes.impl

import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.classes.TTTClassCompanion
import de.moritzruth.spigot_ttt.game.players.TTTPlayerDamageEvent
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class Warrior: TTTClass() {
    override fun init() {
        tttPlayer.walkSpeed -= WALK_SPEED_DECREASE
    }

    override fun reset() {
        tttPlayer.walkSpeed += WALK_SPEED_DECREASE
    }

    companion object: TTTClassCompanion(
        "Warrior",
        ChatColor.BLUE,
        Warrior::class
    ) {
        const val WALK_SPEED_DECREASE = 0.05F

        override val listener = object : Listener {
            @EventHandler(ignoreCancelled = true)
            fun onEntityDamage(event: TTTPlayerDamageEvent) {
                if (event.tttPlayer.tttClass == Warrior) {
                    event.damage *= 0.8
                }
            }
        }
    }
}

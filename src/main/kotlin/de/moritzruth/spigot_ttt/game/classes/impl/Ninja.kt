package de.moritzruth.spigot_ttt.game.classes.impl

import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.classes.TTTClassCompanion
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayerDamageEvent
import de.moritzruth.spigot_ttt.game.players.TTTPlayerReviveEvent
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.nextTick
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class Ninja: TTTClass() {
    var jumpsRemaining = 1
    var checkOnGroundTask: BukkitTask? = null

    override fun init() {
        tttPlayer.player.allowFlight = true
        tttPlayer.player.addPotionEffect(PotionEffect(
            PotionEffectType.JUMP,
            1000000,
            2,
            false,
            false
        ))
    }

    override fun reset() {
        checkOnGroundTask?.cancel()
        checkOnGroundTask = null
    }

    companion object: TTTClassCompanion(
        "Ninja",
        ChatColor.LIGHT_PURPLE,
        Ninja::class
    ) {
        override val listener = object : Listener {
            @EventHandler(ignoreCancelled = true)
            fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
                val tttPlayer = TTTPlayer.of(event.player) ?: return
                val instance = tttPlayer.tttClassInstance
                if (instance !is Ninja) return

                if (event.isFlying) {
                    val vel = tttPlayer.player.velocity
                    tttPlayer.player.velocity = Vector(vel.x * 3, 0.8, vel.z * 3)
                    instance.jumpsRemaining -= 1

                    if (instance.jumpsRemaining == 0) {
                        tttPlayer.player.allowFlight = false

                        instance.checkOnGroundTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
                            if (tttPlayer.player.isOnGround) {
                                instance.jumpsRemaining = 1
                                tttPlayer.player.allowFlight = true
                                instance.reset()
                            }
                        }, 1, 1)
                    }

                    event.isCancelled = true
                }
            }

            @EventHandler(ignoreCancelled = true)
            fun onTTTPlayerDamage(event: TTTPlayerDamageEvent) {
                if (event.tttPlayer.tttClass == Ninja) {
                    if (event.deathReason == DeathReason.FALL) event.damage = 0.0
                }
            }

            @EventHandler
            fun onTTTPlayerReviveEvent(event: TTTPlayerReviveEvent) {
                if (event.tttPlayer.tttClass == Ninja) {
                    // This must be delayed for 2 ticks, idk why
                    nextTick { nextTick { event.tttPlayer.player.allowFlight = true } }
                }
            }
        }
    }
}

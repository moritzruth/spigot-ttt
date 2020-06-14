package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.classes.impl.Stuntman
import de.moritzruth.spigot_ttt.game.items.impl.Fireball
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player

fun createKillExplosion(tttPlayer: TTTPlayer, location: Location, radius: Double) {
    GameManager.world.spawnParticle(
        Particle.EXPLOSION_LARGE,
        location,
        10,
        radius, radius, radius
    )

    GameManager.world.getNearbyEntities(
        location, radius, radius, radius
    ) { it is Player }
        .mapNotNull { TTTPlayer.of(it as Player) }
        .forEach {
            it.damage(
                if (it.tttClass == Stuntman) 0.0 else 1000.0,
                DeathReason.Item(Fireball),
                tttPlayer,
                true
            )
        }
}

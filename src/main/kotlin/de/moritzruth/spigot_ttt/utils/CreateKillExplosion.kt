package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.DamageInfo
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.items.impl.Fireball
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

    val nearbyTTTPlayers = GameManager.world.getNearbyEntities(
        location, radius, radius, radius
    ) { it is Player }.mapNotNull { PlayerManager.getTTTPlayer(it as Player) }

    for (nearbyTTTPlayer in nearbyTTTPlayers) {
        nearbyTTTPlayer.damageInfo = DamageInfo(tttPlayer, DeathReason.Item(Fireball))
        nearbyTTTPlayer.player.damage(20.0)
    }
}

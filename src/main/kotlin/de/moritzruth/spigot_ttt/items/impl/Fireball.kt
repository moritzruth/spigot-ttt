package de.moritzruth.spigot_ttt.items.impl

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.clearHeldItemSlot
import de.moritzruth.spigot_ttt.utils.createKillExplosion
import de.moritzruth.spigot_ttt.utils.isRightClick
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

typealias FireballEntity = org.bukkit.entity.Fireball

object Fireball: TTTItem, Buyable {
    override val type = TTTItem.Type.SPECIAL
    override val itemStack = ItemStack(Material.FIRE_CHARGE).applyMeta {
        setDisplayName("${ChatColor.DARK_RED}${ChatColor.BOLD}Feuerball")

        lore = listOf(
            "",
            "${ChatColor.GOLD}Wirf einen Feuerball"
        )
    }
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL)
    override val price = 1
    override val buyLimit: Int? = null

    val sendersByEntity = mutableMapOf<FireballEntity, TTTPlayer>()

    override val listener = object : Listener {
        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (!event.isRelevant(Fireball)) return
            val player = event.player
            val tttPlayer = PlayerManager.getTTTPlayer(player) ?: return

            if (event.action.isRightClick) {
                player.inventory.clearHeldItemSlot()

                val vector = player.eyeLocation.toVector()
                val location = vector.add(player.eyeLocation.direction.multiply(1.2))
                    .toLocation(player.location.world!!)

                val fireball = GameManager.world.spawnEntity(location, EntityType.FIREBALL) as FireballEntity
                fireball.direction = player.eyeLocation.direction
                sendersByEntity[fireball] = tttPlayer
            }

            event.isCancelled = true
        }

        @EventHandler
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.isRelevant(Fireball)) event.isCancelled = true
        }

        @EventHandler
        fun onExplosionPrime(event: ExplosionPrimeEvent) {
            val sender = sendersByEntity[event.entity]

            if (sender != null) {
                sendersByEntity.remove(event.entity)
                event.isCancelled = true

                GameManager.world.playSound(
                    event.entity.location,
                    Sound.ENTITY_GENERIC_EXPLODE,
                    SoundCategory.PLAYERS,
                    1F,
                    1F
                )
                createKillExplosion(sender, event.entity.location, 2.5)
            }
        }
    }
}

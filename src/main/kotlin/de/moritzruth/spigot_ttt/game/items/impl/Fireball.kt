package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.TTTItemListener
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.game.items.Buyable
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.clearHeldItemSlot
import de.moritzruth.spigot_ttt.utils.createKillExplosion
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.ExplosionPrimeEvent
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

    override val listener = object : TTTItemListener(this, true) {
        override fun onRightClick(data: ClickEventData) {
            data.tttPlayer.player.inventory.clearHeldItemSlot()

            val vector = data.tttPlayer.player.eyeLocation.toVector()
            val location = vector.add(data.tttPlayer.player.eyeLocation.direction.multiply(1.2))
                .toLocation(data.tttPlayer.player.location.world!!)

            val fireball = GameManager.world.spawnEntity(location, EntityType.FIREBALL) as FireballEntity
            fireball.direction = data.tttPlayer.player.eyeLocation.direction
            sendersByEntity[fireball] = data.tttPlayer
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

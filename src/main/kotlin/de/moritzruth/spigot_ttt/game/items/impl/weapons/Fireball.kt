package de.moritzruth.spigot_ttt.game.items.impl.weapons

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.items.ClickEvent
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.clearHeldItemSlot
import de.moritzruth.spigot_ttt.utils.createKillExplosion
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.inventory.ItemStack
import java.util.*

typealias FireballEntity = org.bukkit.entity.Fireball

object Fireball: TTTItem<Fireball.Instance>(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    templateItemStack = ItemStack(Material.FIRE_CHARGE).applyMeta {
        setDisplayName("${ChatColor.DARK_RED}${ChatColor.BOLD}Feuerball")
        lore = listOf(
            "",
            "${ChatColor.GOLD}Wirf einen Feuerball"
        )
    },
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.JACKAL),
        buyLimit = 0,
        price = 1
    )
) {
    class Instance: TTTItem.Instance(Fireball) {
        override fun onRightClick(event: ClickEvent) {
            val carrier = carrier!!
            carrier.player.inventory.clearHeldItemSlot()

            val vector = carrier.player.eyeLocation.toVector()
            val location = vector.add(carrier.player.eyeLocation.direction.multiply(1.2))
                .toLocation(carrier.player.location.world!!)

            val fireball = GameManager.world.spawnEntity(location, EntityType.FIREBALL) as FireballEntity
            fireball.direction = carrier.player.eyeLocation.direction
            sendersByEntity[fireball] = carrier
        }
    }

    val sendersByEntity = WeakHashMap<FireballEntity, TTTPlayer>()

    init {
        addListener(object : TTTItemListener<Instance>(this) {
            @EventHandler
            fun onExplosionPrime(event: ExplosionPrimeEvent) {
                val sender = sendersByEntity[event.entity]

                if (sender != null) {
                    event.isCancelled = true

                    createKillExplosion(sender, event.entity.location, 2.5)
                }
            }
        })
    }
}

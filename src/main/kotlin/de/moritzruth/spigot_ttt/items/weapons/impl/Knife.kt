package de.moritzruth.spigot_ttt.items.weapons.impl

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.TTTItemListener
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.DamageInfo
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.LoreHelper
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.removeTTTItemNextTick
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack

object Knife: TTTItem, Buyable {
    override val itemStack = ItemStack(ResourcePack.Items.knife).applyMeta {
        setDisplayName("${ChatColor.RED}${ChatColor.BOLD}Knife")
        lore = listOf(
            "",
            "${ChatColor.GRAY}Schaden: ${LoreHelper.damage(null)}",
            "",
            "${ChatColor.RED}Nur einmal verwendbar",
            "${ChatColor.RED}Nur aus nächster Nähe"
        )

        hideInfo()
        addAttributeModifier(
            Attribute.GENERIC_ATTACK_SPEED, AttributeModifier(
            "_",
            -0.9,
            AttributeModifier.Operation.ADD_SCALAR
        ))
    }
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL)
    override val price = 1
    override val type = TTTItem.Type.MELEE
    override val buyLimit = 1

    override val listener = object : TTTItemListener(this, false) {
        @EventHandler(ignoreCancelled = true)
        override fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) = handle(event) { damagerTTTPlayer, damagedTTTPlayer ->
            if (event.damage == 1.0) {
                val distance = damagerTTTPlayer.player.location.distance(damagedTTTPlayer.player.location)

                if (distance <= 1.5) {
                    damagedTTTPlayer.damageInfo = DamageInfo(
                        damagerTTTPlayer,
                        DeathReason.Item(Knife),
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                        scream = false
                    )
                    event.damage = 1000.0
                    event.isCancelled = false

                    GameManager.world.playSound(
                        damagedTTTPlayer.player.location,
                        ResourcePack.Sounds.Item.Weapon.Knife.hit,
                        SoundCategory.PLAYERS,
                        1F,
                        1F
                    )

                    damagerTTTPlayer.player.playSound(
                        damagerTTTPlayer.player.location,
                        Sound.ENTITY_ITEM_BREAK,
                        SoundCategory.PLAYERS,
                        1F,
                        1F
                    )

                    damagerTTTPlayer.player.inventory.removeTTTItemNextTick(Knife)
                    return@handle
                }
            }

            event.isCancelled = true
        }
    }
}



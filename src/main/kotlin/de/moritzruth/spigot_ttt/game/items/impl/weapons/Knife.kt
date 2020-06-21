package de.moritzruth.spigot_ttt.game.items.impl.weapons

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.items.LoreHelper
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.hideInfo
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack

object Knife: TTTItem<Knife.Instance>(
    type = Type.MELEE,
    instanceType = Instance::class,
    templateItemStack = ItemStack(Resourcepack.Items.knife).applyMeta {
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
    },
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.JACKAL),
        price = 1,
        buyLimit = 1
    ),
    disableDamage = false
) {
    class Instance: TTTItem.Instance(Knife)

    init {
        addListener(object : TTTItemListener<Instance>(this) {
            @EventHandler(ignoreCancelled = true)
            fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) = handle(event) { damagerTTTPlayer, damagedTTTPlayer ->
                event.isCancelled = true

                if (event.damage == 1.0) {
                    val distance = damagerTTTPlayer.player.location.distance(damagedTTTPlayer.player.location)

                    if (distance <= 1.5) {
                        damagedTTTPlayer.damage(
                            1000.0,
                            DeathReason.Item(Knife),
                            damagerTTTPlayer,
                            false
                        )

                        GameManager.world.playSound(
                            damagedTTTPlayer.player.location,
                            Resourcepack.Sounds.Item.Weapon.Knife.hit,
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

                        damagerTTTPlayer.removeItem(Knife)
                    }
                }
            }
        })
    }
}



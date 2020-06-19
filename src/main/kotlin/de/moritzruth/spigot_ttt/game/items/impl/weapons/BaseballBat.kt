package de.moritzruth.spigot_ttt.game.items.impl.weapons

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.hideInfo
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

object BaseballBat: TTTItem<BaseballBat.Instance>(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    templateItemStack = ItemStack(Resourcepack.Items.baseballBat).applyMeta {
        setDisplayName("${ChatColor.RESET}${ChatColor.BOLD}Baseball-Schläger")
        lore = listOf(
            "",
            "${ChatColor.GOLD}Erhöht die Gechwindigkeit",
            "${ChatColor.GOLD}Schleudert den Gegner weg",
            "",
            "${ChatColor.RED}Nur einmal verwendbar",
            "${ChatColor.RED}Nur aus nächster Nähe"
        )

        hideInfo()
        addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, AttributeModifier(
            "_",
            -0.8,
            AttributeModifier.Operation.ADD_SCALAR
        ))
    },
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.JACKAL),
        price = 1
    ),
    disableDamage = false
) {
    const val WALK_SPEED_INCREASE = 0.1F

    class Instance: TTTItem.Instance(BaseballBat) {
        override fun onSelect() {
            carrier!!.walkSpeed += WALK_SPEED_INCREASE
        }

        override fun onDeselect() {
            carrier!!.walkSpeed -= WALK_SPEED_INCREASE
        }
    }

    override val listener = object : TTTItemListener<Instance>(this) {
        @EventHandler(ignoreCancelled = true)
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) = handle(event) { tttPlayer, _ ->
            event.isCancelled = true
            if (event.damage != 1.0) return@handle // Cooldown on weapon

            val damagedPlayer = event.entity as Player
            val distance = tttPlayer.player.location.distance(damagedPlayer.location)

            if (distance < 2.5) {
                tttPlayer.removeItem(BaseballBat)

                GameManager.world.playSound(
                    damagedPlayer.location,
                    Resourcepack.Sounds.Item.Weapon.BaseballBat.hit,
                    SoundCategory.PLAYERS,
                    1F,
                    1F
                )

                event.damage = 0.0

                val direction = tttPlayer.player.location.direction
                damagedPlayer.velocity = Vector(direction.x * 5, 8.0, direction.z * 5)
            }
        }
    }
}

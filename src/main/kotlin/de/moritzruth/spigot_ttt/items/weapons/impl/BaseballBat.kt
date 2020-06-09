package de.moritzruth.spigot_ttt.items.weapons.impl

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.TTTItemListener
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.Selectable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.removeTTTItemNextTick
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

object BaseballBat: TTTItem, Buyable, Selectable {
    override val type = TTTItem.Type.MELEE
    override val itemStack = ItemStack(ResourcePack.Items.baseballBat).applyMeta {
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
    }
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL)
    override val price = 1
    override val buyLimit: Int? = null

    override fun onSelect(tttPlayer: TTTPlayer) {
        tttPlayer.player.walkSpeed = 0.3F
    }

    override fun onDeselect(tttPlayer: TTTPlayer) {
        tttPlayer.player.walkSpeed = 0.2F
    }

    override val listener = object : TTTItemListener(this, false) {
        @EventHandler(ignoreCancelled = true)
        override fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) = handle(event) { tttPlayer, _ ->
            event.isCancelled = true
            if (event.damage != 1.0) return@handle // Cooldown on weapon

            val damagedPlayer = event.entity as Player
            val distance = tttPlayer.player.location.distance(damagedPlayer.location)

            if (distance < 2.5) {
                tttPlayer.player.inventory.removeTTTItemNextTick(BaseballBat)

                GameManager.world.playSound(
                    damagedPlayer.location,
                    ResourcePack.Sounds.Item.Weapon.BaseballBat.hit,
                    SoundCategory.PLAYERS,
                    1F,
                    1F
                )

                val direction = tttPlayer.player.location.direction
                damagedPlayer.velocity = Vector(direction.x * 5, 8.0, direction.z * 5)
            }
        }
    }
}

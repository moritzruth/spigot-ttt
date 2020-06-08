package de.moritzruth.spigot_ttt.items.weapons.impl

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.Selectable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.removeTTTItemNextTick
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemFlag
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
        addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
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

            val damagedPlayer = event.entity as Player
            val distance = tttPlayer.player.location.distance(damagedPlayer.location)

            if (distance < 2.5) {
                tttPlayer.player.inventory.removeTTTItemNextTick(BaseballBat)

                val direction = tttPlayer.player.location.direction
                damagedPlayer.velocity = Vector(direction.x * 5, 8.0, direction.z * 5)
            }
        }
    }
}

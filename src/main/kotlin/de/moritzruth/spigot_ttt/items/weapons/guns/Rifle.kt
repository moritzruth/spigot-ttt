package de.moritzruth.spigot_ttt.items.weapons.guns.pistol

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.Spawning
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.guns.Gun
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor
import java.util.*

object Rifle: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Rifle",
    damage = heartsToHealth(0.5),
    cooldown = 0.1,
    magazineSize = 40,
    reloadTime = 2.0,
    itemMaterial = CustomItems.rifle,
    recoil = 1
), Buyable, Spawning {
    override val type = TTTItem.Type.HEAVY_WEAPON
    override val price = 1
    override val buyableBy = EnumSet.of(TTTPlayer.Role.TRAITOR)

    class State: Gun.State(magazineSize)
}


